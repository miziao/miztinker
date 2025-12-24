package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.lang.reflect.Constructor;

import static com.mizi.miztinker.miztinker.getResource;

public class Arcana extends NoLevelsModifier implements SlotStackModifierHook, GeneralInteractionModifierHook {

    private static final String MODE_KEY = "arcana_mode";
    private static final String TAG_FORCE_LIGHTNING = "miztinker_force_lightning";

    private static final int FIRE = 0;
    private static final int EXPLOSIVE = 1;
    private static final int WATER = 2;
    private static final int DISCIPLINE = 3;
    private static final int LIGHTNING = 4;

    public Arcana() {
        // 注册全局事件监听，处理闪电落地
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    // --- 核心修复：监听碰撞事件 ---
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        // 检查这个子弹是否带有我们的特殊标记
        if (projectile.getPersistentData().contains(TAG_FORCE_LIGHTNING)) {
            if (!projectile.level().isClientSide) {
                BlockPos pos = BlockPos.containing(event.getRayTraceResult().getLocation());

                // 召唤物理闪电
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(projectile.level());
                if (lightning != null) {
                    lightning.moveTo(Vec3.atBottomCenterOf(pos));
                    if (projectile.getOwner() instanceof Player player) {
                        lightning.setCause((net.minecraft.server.level.ServerPlayer) player);
                    }
                    projectile.level().addFreshEntity(lightning);
                }
            }
        }
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide || source != InteractionSource.RIGHT_CLICK) {
            return InteractionResult.PASS;
        }

        int mode = tool.getPersistentData().getInt(getResource(MODE_KEY));

        String entityPath = switch (mode) {
            case FIRE -> "fire_projectile";
            case EXPLOSIVE -> "lens_projectile";
            case WATER -> "water_projectile";
            case DISCIPLINE -> "homing_arrow";
            case LIGHTNING -> "swrg_projectile";
            default -> "fire_projectile";
        };

        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("projecte", entityPath));

        if (entityType != null) {
            Entity entity = null;
            try {
                if (mode == DISCIPLINE) {
                    Class<?> arrowClass = Class.forName("moze_intel.projecte.gameObjs.entity.EntityHomingArrow");
                    Constructor<?> constructor = arrowClass.getConstructor(net.minecraft.world.level.Level.class, LivingEntity.class, float.class);
                    entity = (Entity) constructor.newInstance(player.level(), player, 100.0F);
                } else if (mode == LIGHTNING) {
                    Class<?> swrgClass = Class.forName("moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile");
                    Constructor<?> constructor = swrgClass.getConstructor(Player.class, boolean.class, net.minecraft.world.level.Level.class);
                    entity = (Entity) constructor.newInstance(player, true, player.level());

                    // --- 关键点：打上特殊标记 ---
                    if (entity != null) {
                        entity.getPersistentData().putBoolean(TAG_FORCE_LIGHTNING, true);
                    }
                } else {
                    entity = entityType.create(player.level());
                }
            } catch (Exception e) {
                entity = entityType.create(player.level());
            }

            if (entity != null) {
                Vec3 lookVec = player.getLookAngle();
                entity.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

                if (entity instanceof Projectile projectile) {
                    projectile.setOwner(player);
                    float velocity = (mode == DISCIPLINE) ? 2.0F : 1.5F;
                    projectile.shoot(lookVec.x, lookVec.y, lookVec.z, velocity, 0.5F);
                }

                player.level().addFreshEntity(entity);

                // 播放音效
                net.minecraft.sounds.SoundEvent sound = switch (mode) {
                    case DISCIPLINE -> net.minecraft.sounds.SoundEvents.ARROW_SHOOT;
                    case LIGHTNING -> net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER;
                    default -> net.minecraft.sounds.SoundEvents.FIRECHARGE_USE;
                };

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        sound, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    // ... overrideOtherStackedOnMe 和 getPriority 保持不变 ...
    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot, Player player, SlotAccess access) {
        ModDataNBT data = tool.getPersistentData();
        int nextMode = (data.getInt(getResource(MODE_KEY)) + 1) % 5;
        data.putInt(getResource(MODE_KEY), nextMode);
        player.displayClientMessage(Component.translatable("modifier.miztinker.arcana.switch")
                .withStyle(ChatFormatting.LIGHT_PURPLE).append(": ")
                .append(Component.translatable("modifier.miztinker.arcana.mode." + nextMode)), true);
        return true;
    }

    @Override
    public int getPriority() { return 60; }
}