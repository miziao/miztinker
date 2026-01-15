package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class GiantKiller extends Modifier implements MeleeDamageModifierHook {

    private static final float ENDERMITE_HEIGHT = 1.8f;
    private static final float BONUS_DAMAGE = 750.0f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    @Override
    public float getMeleeDamage(@NotNull IToolStackView tool, @NotNull ModifierEntry entry,
                                ToolAttackContext context, float baseDamage, float damage) {

        LivingEntity target = context.getLivingTarget();

        if (target != null) {
            float targetHeight = target.getBbHeight();

            if (targetHeight > ENDERMITE_HEIGHT) {
                return damage + (BONUS_DAMAGE * entry.getLevel());
            }
        }

        return damage;
    }
}
