package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class LethalWoundManager {
    private static final Map<UUID, Long> LETHAL_WOUND_EXPIRY = new ConcurrentHashMap<>();

    public static void applyLethalWound(LivingEntity target, int durationTicks) {
        if (target.level().isClientSide) return;

        long expiryTime = System.currentTimeMillis() + (durationTicks * 50L);
        UUID uuid = target.getUUID();

        ForceHurtUtil.makeNoHealable(target);

        LETHAL_WOUND_EXPIRY.put(uuid, expiryTime);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> iterator = LETHAL_WOUND_EXPIRY.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            UUID uuid = entry.getKey();
            long expiryTime = entry.getValue();

            if (currentTime >= expiryTime) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity instanceof LivingEntity living) {
                    ForceHurtUtil.recoverFromNoHealable(living);
                }
                iterator.remove();
            }
        }
    }
}