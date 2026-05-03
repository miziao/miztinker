package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurtWithNoHealable;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.forceSetAllCandidateHealth;

public class AshesSlash extends NoLevelsModifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();

        if (target == null || player == null || player.level().isClientSide) return;

        float threshold = tool.getStats().get(ToolStats.ATTACK_DAMAGE);

        if ((target.getHealth() + damageDealt) <= threshold) {
            executeHardcoreExecution(target);
        }
    }

    private void executeHardcoreExecution(LivingEntity target) {
        ServerLevel level = (ServerLevel) target.level();


        forceHurtWithNoHealable(target, level.damageSources().generic(), target.getHealth());
        forceSetAllCandidateHealth(target, 0F);

        for (int i = 0; i < 20; i++) {
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    target.getRandomX(0.8D), target.getRandomY(), target.getRandomZ(0.8D),
                    1, 0.0D, 0.05D, 0.0D, 0.05D);
        }

        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, target.getSoundSource(), 1.0F, 0.7F);

        target.discard();
    }
}