package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import static com.mizi.miztinker.modifier.register.MiztinkerEffect.HorologiumNoAI;


public class Astral extends Modifier implements MeleeHitModifierHook {

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0) return;

        LivingEntity target = context.getLivingTarget();
        if (target != null && target.isAlive()) {
            int level = modifier.getLevel();
            int duration = 40 + level * 20; // 基础5秒，每级+2秒
            target.addEffect(new MobEffectInstance(HorologiumNoAI.get(), duration, 0, false, true, true));
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, slimeknights.tconstruct.library.modifiers.ModifierHooks.MELEE_HIT);
    }
}