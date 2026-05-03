package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class KingdomCum extends NoLevelsModifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity attacker = context.getAttacker();

        if (attacker.level().isClientSide) return;
        if (tool.isBroken()) return;

        double x, y, z;
        LivingEntity target = context.getLivingTarget();
        if (target != null) {
            x = target.getX();
            y = target.getY();
            z = target.getZ();
        } else {
            Vec3 look = attacker.getLookAngle();
            x = attacker.getX() + look.x * 2.5;
            y = attacker.getY() + attacker.getEyeHeight() + look.y * 2.5;
            z = attacker.getZ() + look.z * 2.5;
        }

        AreaEffectCloud cloud = new AreaEffectCloud(attacker.level(), x, y, z);
        cloud.setOwner(attacker);
        cloud.setRadius(1.5f);
        cloud.setRadiusOnUse(0f);
        cloud.setDuration(300);
        cloud.setWaitTime(0);

        cloud.setFixedColor(0xFFFFFF);
        cloud.setParticle(ParticleTypes.CLOUD);

        int effectDuration = 300;
        cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, effectDuration, 3)); // 缓慢 4
        cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, effectDuration, 9));          // 虚弱 10
        cloud.addEffect(new MobEffectInstance(MobEffects.CONFUSION, effectDuration, 0));         // 反胃
        cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, effectDuration, 0));         // 失明

        attacker.level().playSound(null, x, y, z,
                SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS,
                1.5f, 0.4f);

        attacker.level().addFreshEntity(cloud);
    }
}