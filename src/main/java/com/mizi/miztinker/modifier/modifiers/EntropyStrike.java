package com.mizi.miztinker.modifier.modifiers;


import com.mizi.miztinker.modifier.modifiers.base.DelayedTaskHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class EntropyStrike extends NoLevelsModifier implements MeleeHitModifierHook {

    private static final int DELAY_TICKS = 5;

    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public void afterMeleeHit(IToolStackView tool,
                              ModifierEntry entry,
                              ToolAttackContext context,
                              float damageDealt) {

        if (context.isExtraAttack()) {
            return;
        }

        LivingEntity attacker = context.getAttacker();
        LivingEntity target = context.getLivingTarget();

        if (target == null || target.level().isClientSide) return;


        ServerLevel level = (ServerLevel) target.level();

        float additionalDamage = damageDealt;
        if (additionalDamage <= 0) return;

        DelayedTaskHandler.add(level, DELAY_TICKS, () -> {
            if (!target.isAlive()) return;

            level.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    target.getX(),
                    target.getY(0.5),
                    target.getZ(),
                    15,
                    0.2, 0.2, 0.2,
                    0.1
            );

            DamageSource source = attacker instanceof Player player
                    ? attacker.damageSources().playerAttack(player)
                    : attacker.damageSources().mobAttack(attacker);

            target.invulnerableTime = 0;
            target.hurt(source, additionalDamage);

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.PLAYERS, 1.0F, 1.5F);
        });
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }
}