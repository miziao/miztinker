package com.mizi.miztinker.modifier.modifiers;


import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Dynamax extends Modifier implements MeleeDamageModifierHook {


    /** 计算倍率：基础200倍，然后随等级翻倍 */
    private float getMultiplier(int level) {
        // level = 1 → 200, level = 2 → 400, level = 3 → 800 ...
        return 200F * (float)Math.pow(2, level - 1);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry entry, ToolAttackContext context,
                                float baseDamage, float damage) {
        return damage * getMultiplier(entry.getLevel());
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }
}
