package com.mizi.miztinker.util;

import com.mizi.miztinker.entity.boss.entity.MiziAo;
import com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static com.mizi.miztinker.modifier.modifiers.base.MizUtil.hasAll_Perfect;

public class HealthSafetyManager {

    private static final float MIZI_AO_MAX_HEALTH_CHANGE = 101.0f;
    private static final float PERFECT_IMMUNE_HEALTH_CHANGE = 101.0f;

    public static void handleHealthProtection(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        if (entity.isDeadOrDying()) {
            return;
        }

        if (entity instanceof MiziAo) {
            ForceHurtUtil.applyGenericDamageCap(entity, MIZI_AO_MAX_HEALTH_CHANGE);
            return;
        }

        if (entity instanceof Player player && player.isAddedToWorld() && hasAll_Perfect(player)) {
            ForceHurtUtil.applyGenericDamageImmune(entity, PERFECT_IMMUNE_HEALTH_CHANGE);
            return;
        }

        ForceHurtUtil.uncapDamage(entity);
    }

    public static boolean shouldProtectHealthSafety(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return false;
        }

        if (entity instanceof MiziAo) {
            return true;
        }

        if (entity instanceof Player player) {
            return player.isAddedToWorld() && hasAll_Perfect(player);
        }

        return false;
    }
}