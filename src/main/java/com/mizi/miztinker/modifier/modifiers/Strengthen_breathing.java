package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Strengthen_breathing extends Modifier implements InventoryTickModifierHook, MeleeHitModifierHook {

    /** 每 10 tick 强制满氧气值 */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !isCorrectSlot) return;
        if (!(holder instanceof Player player)) return;

        int modLevel = modifier.getLevel();
        if (modLevel <= 0) return;

        // 每级减少 2 tick 检查间隔，最少为 1 tick
        int checkInterval = Math.max(1, 10 - modLevel * 2);

        if (player.tickCount % checkInterval == 0) {
            int currentAir = player.getAirSupply();
            int maxAir = player.getMaxAirSupply();
            if (currentAir < maxAir) {
                player.setAirSupply(maxAir);
            }
        }
    }

    /** 攻击命中后造成额外 30% 窒息伤害 */
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier,
                              ToolAttackContext context, float damageDealt) {
        if (!context.isFullyCharged()) return;

        LivingEntity attacker = context.getAttacker();
        LivingEntity target = context.getLivingTarget();
        if (target == null || !(attacker.level() instanceof ServerLevel serverLevel)) return;

        // 造成额外 30% 的窒息伤害
        float extraDamage = damageDealt * 0.3f;

        // 创建 DamageSource：DROWN
        DamageSource drownSource = new DamageSource(
                serverLevel.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.DROWN),
                attacker
        );

        // 造成附加窒息伤害
        target.hurt(drownSource, extraDamage);
    }

    /** 注册钩子 */
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }
}