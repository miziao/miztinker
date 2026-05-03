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

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurt;

public class AwakenUltraman extends NoLevelsModifier implements
        EquipmentChangeModifierHook,
        RequirementsModifierHook,
        ValidateModifierHook,
        InventoryTickModifierHook,
        KeybindInteractModifierHook {

    private static final int MAX_CHARGE_TICKS = 160;  // 8 秒
    private static final float MAX_SCALE = 7.0f;
    private static final int BEAM_DAMAGE = 35;

    private static final ResourceLocation CHARGE_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "ultraman_charge");
    private static final ResourceLocation GIANT_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "ultraman_giant");

    private static final UUID HEALTH_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
        hookBuilder.addHook(this, ModifierHooks.ARMOR_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.REQUIREMENTS);
        hookBuilder.addHook(this, ModifierHooks.VALIDATE);
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

    /* ===== 变身 / 取消（通过盔甲交互键触发） ===== */
    @Override
    public boolean startInteract(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot slot, TooltipKey key) {
        CompoundTag data = player.getPersistentData();
        boolean isGiant = data.getBoolean(GIANT_KEY.toString());

        if (!isGiant) {
            toggleScale(player);
        } else {
            resetScaleAndHealth(player);
            data.putInt(CHARGE_KEY.toString(), 0);
        }
        return true;
    }

    @Override
    public void stopInteract(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot slot) {}

    /* ===== Tick 逻辑（注意：只有当该工具在“正确槽位”时才会被调用） ===== */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier,
                                Level level, LivingEntity living,
                                int slot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {

        // 仅服务端玩家并且确实在“正确槽位”（即装备栏）才生效
        if (!(living instanceof Player player)) return;
        if (!isCorrectSlot || level.isClientSide()) return;

        CompoundTag data = player.getPersistentData();

        // 只有装备在盔甲栏位时才允许飞行（持续设置确保状态不会残留）
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();

        boolean isGiant = data.getBoolean(GIANT_KEY.toString());
        boolean isShift = player.isShiftKeyDown();

        /* === 巨化 → 加上抗性 III === */
        if (isGiant) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 3, true, false, false));
        }

        /* === 蓄力光线 === */
        if (isGiant && isShift) {
            int ticks = data.getInt(CHARGE_KEY.toString()) + 1;
            data.putInt(CHARGE_KEY.toString(), ticks);

            // 显示剩余秒数（客户端提示）
            player.displayClientMessage(Component.literal("§b蓄力中: " + Math.max(0, (MAX_CHARGE_TICKS - ticks) / 20) + "秒"), true);

            // 蓄满发射（仅服务端真正发射）
            if (ticks >= MAX_CHARGE_TICKS && level instanceof ServerLevel server)
                shootBeam(server, player);

        } else {
            data.putInt(CHARGE_KEY.toString(), 0);
        }
    }

    /* ===== 发射光线（服务端） ===== */
    private void shootBeam(ServerLevel level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        Set<LivingEntity> hitEntities = new HashSet<>();
        int steps = 120;

        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.0f, 1.0f);

        for (int i = 0; i < steps; i++) {
            Vec3 pos = start.add(look.scale(i * 0.5));

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 10, 0.3, 0.3, 0.3, 0.05);
            level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 5, 0.2, 0.2, 0.2, 0.05);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 7, 0.3, 0.3, 0.3, 0.5);

            AABB box = new AABB(pos.subtract(1.5, 1.5, 1.5), pos.add(1.5, 1.5, 1.5));

            List<LivingEntity> nearby = level.getEntitiesOfClass(
                    LivingEntity.class, box,
                    e -> e != player && e.isAlive() && !hitEntities.contains(e)
            );

            for (LivingEntity target : nearby) {
                forceHurt(target, player.damageSources().generic(), BEAM_DAMAGE);
                hitEntities.add(target);
            }
        }
    }

    /* ===== 巨化开关 ===== */
    private void toggleScale(Player player) {
        CompoundTag tag = player.getPersistentData();
        boolean isGiant = tag.getBoolean(GIANT_KEY.toString());

        ScaleData scale = ScaleTypes.BASE.getScaleData(player);
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);

        if (!isGiant) {
            // 放大
            scale.setTargetScale(MAX_SCALE);

            if (health != null && health.getModifier(HEALTH_UUID) != null) {
                health.removeModifier(HEALTH_UUID);
            }

            if (health != null) {
                health.addTransientModifier(new AttributeModifier(
                        HEALTH_UUID, "Ultraman Health", 1000, AttributeModifier.Operation.ADDITION));
            }

            player.setHealth(player.getMaxHealth());
            tag.putBoolean(GIANT_KEY.toString(), true);

        } else {
            resetScaleAndHealth(player);
        }
    }

    /* ===== 恢复规模 / 血量（注意：这里要关闭 mayfly，防止残留） ===== */
    private void resetScaleAndHealth(Player player) {
        ScaleData scale = ScaleTypes.BASE.getScaleData(player);
        scale.setTargetScale(1.0f);

        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_UUID) != null) {
            health.removeModifier(HEALTH_UUID);
            if (player.getHealth() > player.getMaxHealth())
                player.setHealth(player.getMaxHealth());
        }

        // 移除巨化标记
        player.getPersistentData().remove(GIANT_KEY.toString());

        // 重置飞行权限：恢复为不可飞（默认由 onEquip/onInventoryTick 控制）
        // 如果玩家在创造/旁观则保持允许（但一般这里我们统一关闭然后由装备判定开启）
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    /* ===== 装备栏事件（装备） ===== */
    @Override
    public void onEquip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        // 仅当装备在盔甲槽才赋予 mayfly（避免其它槽位误触发）
        if (!(context.getEntity() instanceof ServerPlayer player)) return;

        // 如果替换物仍然有该 modifier，则不关闭飞行
        IToolStackView replacement = context.getReplacementTool();
        if (replacement != null && replacement.getModifierLevel(this) > 0) {
            return;
        }

        // 保护创意/旁观模式玩家
        GameType gm = player.gameMode.getGameModeForPlayer();
        if (gm == GameType.SPECTATOR || gm == GameType.CREATIVE) return;

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
    }

    /* ===== 装备栏事件（脱下） ===== */
    @Override
    public void onUnequip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;

        // 如果替换物仍然带有该 modifier，则不移除权限（比如换了一件也带有特性的盔甲）
        IToolStackView replacement = context.getReplacementTool();
        if (replacement != null && replacement.getModifierLevel(this) > 0) return;

        // 只有在非创造/旁观模式下关闭飞行权限
        GameType gm = player.gameMode.getGameModeForPlayer();
        if (gm != GameType.CREATIVE && gm != GameType.SPECTATOR) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        // 同步重置所有巨化相关状态，防止残留
        resetScaleAndHealth(player);
        // 清除蓄力计数
        player.getPersistentData().putInt(CHARGE_KEY.toString(), 0);
    }
}
