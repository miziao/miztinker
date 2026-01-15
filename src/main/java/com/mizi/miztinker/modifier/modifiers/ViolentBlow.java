package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class ViolentBlow extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0) return;

        LivingEntity target = context.getLivingTarget();
        if (target != null && target.isAlive()) {
            int modifierLevel = modifier.getLevel();
            int duration = modifierLevel * 10 * 20;
            int maxAmplifier = (modifierLevel * 5) - 1;

            MobEffectInstance currentEffect = target.getEffect(MiztinkerEffect.BoneFractureEffect.get());

            int newAmplifier = 0;
            if (currentEffect != null) {
                newAmplifier = Math.max(currentEffect.getAmplifier(), Math.min(maxAmplifier, currentEffect.getAmplifier() + 1));
            }

            target.addEffect(new MobEffectInstance(MiztinkerEffect.BoneFractureEffect.get(), duration, newAmplifier));
        }
    }
}