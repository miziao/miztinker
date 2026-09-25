package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class DeathSafetyEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        if (!ForceHurtUtil.shouldCancelProtectedDeath(entity)) {
            return;
        }

        event.setCanceled(true);

        entity.deathTime = 0;
        entity.hurtTime = 0;
        entity.hurtDuration = 0;
        entity.invulnerableTime = 20;

        entity.setHealth(ForceHurtUtil.getProtectedSafeHealth(entity));
    }
}