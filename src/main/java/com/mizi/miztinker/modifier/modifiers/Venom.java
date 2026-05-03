package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Venom extends NoLevelsModifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();

        if (attacker.level().isClientSide || target == null || !target.isAlive()) {
            return;
        }

        MobEffectInstance currentPoison = target.getEffect(MobEffects.POISON);

        int newAmplifier = 0;
        int newDuration = 200;

        if (currentPoison != null) {
            newAmplifier = currentPoison.getAmplifier() + 1;
            newDuration = currentPoison.getDuration() + 200;
        }

        newAmplifier = Math.min(newAmplifier, 254);

        target.addEffect(new MobEffectInstance(MobEffects.POISON, newDuration, newAmplifier), attacker);
    }
}