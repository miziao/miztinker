package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import net.minecraft.world.item.TooltipFlag;

public class DeceiveDevil extends NoLevelsModifier implements GeneralInteractionModifierHook, TooltipModifierHook {

    private static final ResourceLocation COORD_X = new ResourceLocation("modid", "deceive_devil_x");
    private static final ResourceLocation COORD_Y = new ResourceLocation("modid", "deceive_devil_y");
    private static final ResourceLocation COORD_Z = new ResourceLocation("modid", "deceive_devil_z");
    private static final ResourceLocation DIMENSION = new ResourceLocation("modid", "deceive_devil_dim");

    public DeceiveDevil() {
        // 注册死亡事件监听器
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player,
                                       InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide) return InteractionResult.PASS; // 只在服务器执行

        if (source == InteractionSource.RIGHT_CLICK && player.isCrouching() && !tool.isBroken()) {
            ModDataNBT data = tool.getPersistentData();
            data.putFloat(COORD_X, (float) player.getX());
            data.putFloat(COORD_Y, (float) player.getY());
            data.putFloat(COORD_Z, (float) player.getZ());
            data.putString(DIMENSION, player.level().dimension().location().toString());

            player.sendSystemMessage(Component.literal("坐标已绑定!"));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ModifiableItem)) continue;

            ToolStack tool = ToolStack.from(stack);
            if (tool.getModifierLevel(getId()) <= 0) continue;

            ModDataNBT data = tool.getPersistentData();
            if (!data.contains(COORD_X) || !data.contains(COORD_Y) || !data.contains(COORD_Z)) continue;

            float x = data.getFloat(COORD_X);
            float y = data.getFloat(COORD_Y);
            float z = data.getFloat(COORD_Z);
            String dimString = data.getString(DIMENSION);

            if ((x != 0 || y != 0 || z != 0) && player.getInventory().countItem(Items.GOLD_INGOT) >= 66) {
                // 阻止死亡流程
                event.setCanceled(true);

                // 保命 + 无敌几秒避免连环死
                player.setHealth(player.getMaxHealth() * 0.2f);
                player.invulnerableTime = 60; // 1.20.x 的字段名是 public invulnerableTime

                // 清空最后攻击来源（避免再次触发死亡）
                player.setLastHurtByPlayer(null);
                player.setLastHurtByMob(null);

                // 消耗 66 个金锭
                int remaining = 66;
                for (int i = 0; i < player.getInventory().items.size() && remaining > 0; i++) {
                    ItemStack invStack = player.getInventory().items.get(i);
                    if (invStack.getItem() == Items.GOLD_INGOT) {
                        int remove = Math.min(invStack.getCount(), remaining);
                        invStack.shrink(remove);
                        remaining -= remove;
                    }
                }

                player.displayClientMessage(Component.literal("§c你已逃脱死神的魔爪，现在要收取费用了"), true);

                // 获取目标维度
                ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimString));
                ServerLevel targetLevel = player.server.getLevel(targetKey);

                if (targetLevel != null) {
                    // ✅ Forge 1.20.x 的跨维传送正确写法
                    player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());
                    // 传送特效
                    targetLevel.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 50, 0.5, 0.5, 0.5, 0.1);
                    targetLevel.playSound(null, x, y, z,
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                } else {
                    // 若维度无效，回到主世界安全点
                    ServerLevel overworld = player.server.overworld();
                    player.teleportTo(overworld,
                            overworld.getSharedSpawnPos().getX() + 0.5,
                            overworld.getSharedSpawnPos().getY(),
                            overworld.getSharedSpawnPos().getZ() + 0.5,
                            0.0f, 0.0f);
                    player.displayClientMessage(Component.literal("§7契约破裂，你被抛回了主世界。"), true);
                }

                return; // 结束逻辑
            }
        }
    }
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT, ModifierHooks.TOOLTIP);
    }

    @Override
    public void addTooltip(IToolStackView iToolStackView, ModifierEntry modifierEntry, @Nullable Player player,
                           List<Component> list, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = iToolStackView.getPersistentData();
        if (data.contains(COORD_X) && data.contains(COORD_Y) && data.contains(COORD_Z) && data.contains(DIMENSION)) {
            float x = data.getFloat(COORD_X);
            float y = data.getFloat(COORD_Y);
            float z = data.getFloat(COORD_Z);
            String dim = data.getString(DIMENSION);

            list.add(Component.literal(String.format("绑定坐标: %.1f, %.1f, %.1f", x, y, z)));
            list.add(Component.literal("维度: " + dim));
        } else {
            list.add(Component.literal("未绑定坐标"));
        }
    }
        }

