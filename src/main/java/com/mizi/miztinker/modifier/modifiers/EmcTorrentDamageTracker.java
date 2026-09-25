package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "miztinker")
public class EmcTorrentDamageTracker {

    private static final Map<UUID, DamageRecord> FINAL_DAMAGE_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, DamageRecord> THEORETICAL_DAMAGE_CACHE = new ConcurrentHashMap<>();

    private static class DamageRecord {
        float damage;
        long gameTime;

        DamageRecord(float damage, long gameTime) {
            this.damage = damage;
            this.gameTime = gameTime;
        }
    }

    public static void cacheTheoreticalDamage(ServerPlayer player, float damage) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        if (damage <= 0.0f || Float.isNaN(damage) || Float.isInfinite(damage)) {
            return;
        }

        THEORETICAL_DAMAGE_CACHE.put(
                player.getUUID(),
                new DamageRecord(damage, player.level().getGameTime())
        );
    }

    public static float popTheoreticalDamage(ServerPlayer player) {
        if (player == null) {
            return 0.0f;
        }

        DamageRecord record = THEORETICAL_DAMAGE_CACHE.remove(player.getUUID());

        if (record == null) {
            return 0.0f;
        }

        long now = player.level().getGameTime();

        if (Math.abs(now - record.gameTime) > 1) {
            return 0.0f;
        }

        return Math.max(0.0f, record.damage);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        Entity sourceEntity = event.getSource().getEntity();

        if (!(sourceEntity instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!EMC_torrent_hasModifier.hasEmcTorrent(player)) {
            return;
        }

        float amount = event.getAmount();

        if (amount <= 0.0f || Float.isNaN(amount) || Float.isInfinite(amount)) {
            return;
        }

        long gameTime = player.level().getGameTime();
        UUID uuid = player.getUUID();

        FINAL_DAMAGE_CACHE.compute(uuid, (key, old) -> {
            if (old == null || old.gameTime != gameTime) {
                return new DamageRecord(amount, gameTime);
            }

            old.damage += amount;
            return old;
        });
    }

    public static float popFinalDamage(ServerPlayer player) {
        if (player == null) {
            return 0.0f;
        }

        DamageRecord record = FINAL_DAMAGE_CACHE.remove(player.getUUID());

        if (record == null) {
            return 0.0f;
        }

        long now = player.level().getGameTime();

        if (Math.abs(now - record.gameTime) > 1) {
            return 0.0f;
        }

        return Math.max(0.0f, record.damage);
    }
}