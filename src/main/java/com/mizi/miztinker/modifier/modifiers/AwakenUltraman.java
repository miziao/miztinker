package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.RequirementsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.KeybindInteractModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.modifierSeverance;

public class AwakenUltraman extends NoLevelsModifier implements
        EquipmentChangeModifierHook,
        RequirementsModifierHook,
        ValidateModifierHook,
        InventoryTickModifierHook,
        KeybindInteractModifierHook {

    private static final int MAX_CHARGE_TICKS = 160;  // 8秒
    private static final float MAX_SCALE = 7.0f;
    private static final int BEAM_DAMAGE = 700;
    private static final int FIRING_COOLDOWN_KEY = 10;   // 0.5秒 = 10 tick
    private static final ResourceLocation CHARGE_KEY = new ResourceLocation("miztinker", "ultraman_charge");
    private static final ResourceLocation GIANT_KEY = new ResourceLocation("miztinker", "ultraman_giant");
    private static final UUID HEALTH_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");


    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
        hookBuilder.addHook(this, ModifierHooks.ARMOR_INTERACT);
    }


    @Override
    public @Nullable Component validate(IToolStackView tool, ModifierEntry entry) {
        if (tool.getModifierLevel(MiztinkerModifiers.ULTRAMAN.getId()) > 0)
            return null;
        return requirementsError(entry);
    }

    @Override
    public Component requirementsError(ModifierEntry entry) {
        return Component.translatable("modifier.miztinker.awakenultraman.requirements");
    }

    @Override
    public boolean startInteract(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot slot, TooltipKey key) {
        CompoundTag data = player.getPersistentData();
        boolean isGiant = data.getBoolean(GIANT_KEY.toString());

        if (!isGiant) {
            toggleScale(player); // 变身
        } else {
            resetScaleAndHealth(player); // 取消变身
            // 重置蓄力和冷却
            data.putInt(CHARGE_KEY.toString(), 0);
            data.putInt(String.valueOf(FIRING_COOLDOWN_KEY), 0);
        }

        return true; // 事件已处理
    }

    @Override
    public void stopInteract(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot slot) {
        // 按键松开不做额外操作
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier,
                                Level level, LivingEntity living,
                                int slot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(living instanceof Player player) || level.isClientSide || !isCorrectSlot) return;

        CompoundTag data = player.getPersistentData();
        player.getAbilities().mayfly = true;

        boolean isGiant = data.getBoolean(GIANT_KEY.toString());
        boolean isShift = player.isShiftKeyDown(); // 下蹲判断

        // 巨大化持续抗性效果
        if (isGiant) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 3, true, false, false));
        }

        // 光线蓄力逻辑（必须巨化且下蹲才生效）
        if (isGiant && isShift) {
            int chargeTicks = data.getInt(CHARGE_KEY.toString()) + 1;
            data.putInt(CHARGE_KEY.toString(), chargeTicks);

            int secondsLeft = Math.max(0, (MAX_CHARGE_TICKS - chargeTicks) / 20);
            player.displayClientMessage(Component.literal("§b蓄力中: " + secondsLeft + "秒"), true);

            int cooldown = data.getInt(String.valueOf(FIRING_COOLDOWN_KEY));
            if (chargeTicks >= MAX_CHARGE_TICKS && cooldown <= 0) {
                if (level instanceof ServerLevel serverLevel) {
                    shootBeam(serverLevel, player);
                }
                data.putInt(String.valueOf(FIRING_COOLDOWN_KEY), 10);
            } else if (cooldown > 0) {
                data.putInt(String.valueOf(FIRING_COOLDOWN_KEY), cooldown - 1);
            }
        } else {
            // 玩家没有下蹲或未巨化 → 重置蓄力计数和冷却
            data.putInt(CHARGE_KEY.toString(), 0);
            data.putInt(String.valueOf(FIRING_COOLDOWN_KEY), 0);
        }
    }

    @Override
    public void onEquip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;
        player.getAbilities().mayfly = true;
    }

    @Override
    public void onUnequip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;

        IToolStackView replacement = context.getReplacementTool();
        if (replacement != null && replacement.getModifierLevel(this) > 0) return;

        GameType gm = player.gameMode.getGameModeForPlayer();
        if (gm != GameType.SPECTATOR && gm != GameType.CREATIVE) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
        }

        resetScaleAndHealth(player);
    }

    private void shootBeam(ServerLevel level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Set<LivingEntity> hitEntities = new HashSet<>();
        int steps = 120;

        // 信标音效
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.0f, 1.0f);

        for (int i = 0; i < steps; i++) {
            Vec3 pos = start.add(look.scale(i * 0.5));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.05);
            level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 5, 0.2, 0.2, 0.2, 0.05);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 7, 0.3, 0.3, 0.3, 0.5);

            AABB box = new AABB(pos.subtract(1.5, 1.5, 1.5), pos.add(1.5, 1.5, 1.5));
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive() && !hitEntities.contains(e));

            for (LivingEntity target : nearby) {
                modifierSeverance(target, player, BEAM_DAMAGE, 1.0f, 0.0f);
                hitEntities.add(target);
            }
        }
    }

    private void toggleScale(Player player) {
        CompoundTag tag = player.getPersistentData();
        boolean isGiant = tag.getBoolean(GIANT_KEY.toString());

        ScaleData scale = ScaleTypes.BASE.getScaleData(player);
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;

        // 保存原本飞行状态
        boolean wasFlying = player.getAbilities().flying;
        boolean canFly = player.getAbilities().mayfly;

        if (!isGiant) {
            scale.setTargetScale(MAX_SCALE);

            // 移除已有的同 UUID modifier 避免重复
            if (health.getModifier(HEALTH_UUID) != null) {
                health.removeModifier(HEALTH_UUID);
            }
            health.addTransientModifier(new AttributeModifier(HEALTH_UUID, "Ultraman Health", 1000, AttributeModifier.Operation.ADDITION));
            player.setHealth(player.getMaxHealth());
            tag.putBoolean(GIANT_KEY.toString(), true);

            // 恢复飞行
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = wasFlying;
            player.onUpdateAbilities();
        } else {
            resetScaleAndHealth(player);
        }
    }

    private void resetScaleAndHealth(Player player) {
        ScaleData scale = ScaleTypes.BASE.getScaleData(player);
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_UUID) != null) {
            health.removeModifier(HEALTH_UUID);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
        scale.setTargetScale(1.0f);

        CompoundTag tag = player.getPersistentData();
        tag.remove(GIANT_KEY.toString());

        // 恢复飞行状态（可根据需求调整）
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }
}