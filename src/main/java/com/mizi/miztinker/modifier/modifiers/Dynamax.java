package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Dynamax extends Modifier implements MeleeDamageModifierHook, MeleeHitModifierHook {

    public static final ResourceLocation DYNAMAX_MULT = ResourceLocation.fromNamespaceAndPath("miztinker", "dynamax_multiplier");

    private float getMultiplier(int level) {
        return 200F * (float)Math.pow(2, level - 1);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float baseDamage, float damage) {
        float multiplier = getMultiplier(entry.getLevel());
        tool.getPersistentData().putFloat(DYNAMAX_MULT, multiplier);
        return damage * multiplier;
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        tool.getPersistentData().remove(DYNAMAX_MULT);
    }

    @Override
    public void failedMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageAttempted) {
        tool.getPersistentData().remove(DYNAMAX_MULT);
    }
}