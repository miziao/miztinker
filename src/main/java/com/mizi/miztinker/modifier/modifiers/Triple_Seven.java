package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.util.RandomSource;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Triple_Seven extends NoLevelsModifier implements MeleeDamageModifierHook {

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry entry, ToolAttackContext context,
                                float baseDamage, float damage) {
        // 如果没有目标或不是暴击，保持原伤害
        if (context.getLivingTarget() == null) return damage;
        if (!context.isCritical()) return damage;

        // 随机 7 ~ 777（包含两端）
        RandomSource random = context.getLevel().getRandom();
        int multiplier = 7 + random.nextInt(777 - 7 + 1); // nextInt(771) 的更直观替代

        return damage * (float) multiplier;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }
}