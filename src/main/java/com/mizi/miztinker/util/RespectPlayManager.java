package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class RespectPlayManager {
    private static final Map<UUID, Long> RESPECT_PLAY_EXPIRY = new ConcurrentHashMap<>();

    public static void applyRespectPlay(LivingEntity target, int durationTicks) {
        if (target.level().isClientSide) return;

        long expiryTime = System.currentTimeMillis() + (durationTicks * 50L);
        RESPECT_PLAY_EXPIRY.put(target.getUUID(), expiryTime);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        boolean hasEffect = entity.hasEffect(MiztinkerEffect.RESPECTPLAY.get());
        boolean hasLogicRecord = RESPECT_PLAY_EXPIRY.containsKey(entity.getUUID());

        if (hasEffect || hasLogicRecord) {
            entity.invulnerableTime = 0;

            if (hasEffect) {
                applyRespectPlay(entity, 20);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        if (!(event.level instanceof ServerLevel)) return;

        long currentTime = System.currentTimeMillis();

        RESPECT_PLAY_EXPIRY.entrySet().removeIf(entry -> currentTime >= entry.getValue());
    }
}