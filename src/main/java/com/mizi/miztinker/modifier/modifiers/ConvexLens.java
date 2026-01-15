package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class ConvexLens extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (context.getTarget() instanceof LivingEntity target && !target.level().isClientSide) {

            List<MobEffectInstance> debuffs = target.getActiveEffects().stream()
                    .filter(effect -> !effect.getEffect().isBeneficial())
                    .toList();

            if (debuffs.isEmpty()) return;

            MobEffectInstance selectedInstance = debuffs.get(target.getRandom().nextInt(debuffs.size()));

            int currentAmplifier = selectedInstance.getAmplifier();
            int bonus = modifier.getLevel();
            int newAmplifier = currentAmplifier + bonus;

            MobEffectInstance amplifiedEffect = new MobEffectInstance(
                    selectedInstance.getEffect(),
                    selectedInstance.getDuration(),
                    newAmplifier,
                    selectedInstance.isAmbient(),
                    selectedInstance.isVisible(),
                    selectedInstance.showIcon()
            );

            target.addEffect(amplifiedEffect);
        }
    }
}