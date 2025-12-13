package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;


public class Mercy extends NoLevelsModifier implements MeleeDamageModifierHook {

    private static final float MIN_HEALTH = 1.0f;

    @Override
    protected void registerHooks(@NotNull ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public int getPriority() {
        return 100; // 在所有伤害计算最后执行
    }

    @Override
    public float getMeleeDamage(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier,
                                ToolAttackContext context, float baseDamage, float damage) {

        LivingEntity target = context.getLivingTarget();
        if (target == null || target.isDeadOrDying() || target.isInvulnerable()) {
            return damage;
        }

        float currentHealth = target.getHealth();

        // 如果会被击杀，则把伤害调整到只剩 1 点生命
        if (currentHealth - damage <= 0.0f) {
            return Math.max(currentHealth - MIN_HEALTH, 0f);
        }

        return damage;
    }
}