package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurt;

public class Agni extends NoLevelsModifier implements InventoryTickModifierHook, EquipmentChangeModifierHook {

    /**
     * 伤害冷却：每 10 tick 触发一次伤害（0.5 秒）
     */
    private static final int DAMAGE_COOLDOWN_TICKS = 10;
    /**
     * 伤害数值
     */
    private static final float SELF_DAMAGE = 10f;
    private static final float SPREAD_DAMAGE = 10f;
    /**
     * 火焰传播半径
     */
    private static final double RADIUS = 1.5;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
    }

    /* ===== 盔甲栏检测与激活 ===== */
    @Override
    public void onEquip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        // 无需额外处理
    }

    @Override
    public void onUnequip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        // 无需做任何事（火会在 Tick 时被重新添加）
    }

    /* ===== 主逻辑：持续火焰、治疗、自伤、扩散、点火 ===== */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier,
                                Level level, LivingEntity holder,
                                int slot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (!(holder instanceof ServerPlayer player)) return;
        if (!isCorrectSlot) return;
        if (level.isClientSide()) return;

        ServerLevel server = (ServerLevel) level;

        // 玩家持续治疗
        player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 100, true, false, false));

        // 玩家自伤逻辑（与着火无关）
        String damageCooldownKey = "agniDamageCooldown";
        int cooldown = player.getPersistentData().getInt(damageCooldownKey);

        if (cooldown <= 0) {
            forceHurt(player, player.damageSources().generic(), SELF_DAMAGE);
            player.getPersistentData().putInt(damageCooldownKey, DAMAGE_COOLDOWN_TICKS);
        } else {
            player.getPersistentData().putInt(damageCooldownKey, cooldown - 1);
        }

        // 玩家持续燃烧（仅在非水中）
        if (!player.isInWaterOrRain() && !player.isOnFire()) {
            player.setSecondsOnFire(9999);
        }

        // 周围生物持续燃烧
        List<LivingEntity> nearby = server.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(RADIUS),
                e -> e != player && e.isAlive()
        );
        for (LivingEntity target : nearby) {
            if (!target.isInWaterOrRain()) {
                target.setSecondsOnFire(1);
            }
            // 扩散伤害（每 DAMAGE_COOLDOWN_TICKS）
            if (cooldown <= 0) {
                forceHurt(target, player.damageSources().generic(), SPREAD_DAMAGE);
            }
        }

        // 脚下点火
        BlockPos pos = player.blockPosition();
        if (server.isEmptyBlock(pos)) {
            server.setBlock(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), 3);
        }
    }
}