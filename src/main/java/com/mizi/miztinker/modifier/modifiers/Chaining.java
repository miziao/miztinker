package com.mizi.miztinker.modifier.modifiers;

import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Chaining extends NoLevelsModifier implements MeleeDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.@NotNull Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float baseDamage, float damage) {
        int activeDamageHooks = 0;

        for (ModifierEntry modEntry : tool.getModifierList()) {
            if (modEntry.getModifier() != this) {
                var hook = modEntry.getHook(ModifierHooks.MELEE_DAMAGE);
                if (hook != ModifierHooks.MELEE_DAMAGE.getDefaultInstance()) {
                    activeDamageHooks++;
                }
            }
        }

        if (activeDamageHooks >= 3) {
            damage *= activeDamageHooks;
        }
        return damage;
    }
}