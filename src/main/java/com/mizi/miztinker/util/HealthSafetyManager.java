package com.mizi.miztinker.util;

import com.mizi.miztinker.entity.boss.entity.MiziAo;
import com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import static com.mizi.miztinker.modifier.modifiers.base.MizUtil.hasAll_Perfect;

public class HealthSafetyManager {

    public static void handleHealthProtection(LivingEntity entity) {
        if (entity.level().isClientSide() || entity.isDeadOrDying()) {
            return;
        }

        boolean shouldProtect = false;

        if (entity instanceof MiziAo) {
            shouldProtect = true;
        } else if (entity instanceof Player player && player.isAddedToWorld()) {
            if (hasAll_Perfect(player)) {
                shouldProtect = true;
            }
        }

        if (shouldProtect) {
            ForceHurtUtil.applyGenericDamageCap(entity, 101.0f);
        } else {
            ForceHurtUtil.uncapDamage(entity);
        }
    }
}