package com.mizi.miztinker.util;

import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.TimeStopPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MizTimeStopHandler {
    private static final Set<ResourceLocation> STOPPED_DIMENSIONS = new HashSet<>();
    private static final Set<UUID> EXEMPT_ENTITIES = new HashSet<>();
    private static final Map<UUID, Vec3> VELOCITY_CACHE = new ConcurrentHashMap<>();

    public static void toggle(Entity owner, boolean active) {
        ResourceLocation dim = owner.level().dimension().location();
        if (active) {
            STOPPED_DIMENSIONS.add(dim);
            EXEMPT_ENTITIES.add(owner.getUUID());
        } else {
            STOPPED_DIMENSIONS.remove(dim);
            EXEMPT_ENTITIES.remove(owner.getUUID());
            restoreVelocities(owner.level());
        }
    }

    private static void restoreVelocities(net.minecraft.world.level.Level level) {
        if (level instanceof ServerLevel serverLevel) {
            for (Map.Entry<UUID, Vec3> entry : VELOCITY_CACHE.entrySet()) {
                Entity entity = serverLevel.getEntity(entry.getKey());
                if (entity != null) {
                    entity.setDeltaMovement(entry.getValue());
                    entity.setNoGravity(false);
                    entity.hurtMarked = true;
                    entity.setNoGravity(false);
                }
            }
            VELOCITY_CACHE.clear();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        if (EXEMPT_ENTITIES.contains(uuid)) {
            toggle(event.getEntity(), false);
            MiztinkerNetwork.sendToClient(new TimeStopPacket(false));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        handleFreeze(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.level.isClientSide) return;

        if (STOPPED_DIMENSIONS.contains(event.level.dimension().location())) {
            for (Entity entity : event.level.getEntities().getAll()) {
                if (!(entity instanceof LivingEntity)) {
                    handleFreeze(entity);
                }
            }
        }
    }

    private static void handleFreeze(Entity entity) {
        if (entity == null || entity.level().isClientSide) return;

        if (STOPPED_DIMENSIONS.contains(entity.level().dimension().location())) {
            UUID uuid = entity.getUUID();
            if (!EXEMPT_ENTITIES.contains(uuid)) {
                if (!VELOCITY_CACHE.containsKey(uuid)) {
                    VELOCITY_CACHE.put(uuid, entity.getDeltaMovement());
                }

                entity.setPos(entity.getX(), entity.getY(), entity.getZ());
                entity.xo = entity.getX();
                entity.yo = entity.getY();
                entity.zo = entity.getZ();

                entity.setDeltaMovement(Vec3.ZERO);
                entity.setNoGravity(true);
                entity.hurtMarked = true;

                if (entity instanceof Projectile p) {
                    if (p.tickCount > 0) p.tickCount--;
                }
            }
        }
    }

    public static boolean isEntityStopped(Entity entity) {
        if (entity == null) return false;
        ResourceLocation dim = entity.level().dimension().location();
        return STOPPED_DIMENSIONS.contains(dim) && !EXEMPT_ENTITIES.contains(entity.getUUID());
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (isEntityStopped(event.getSource().getEntity())) {
            event.setCanceled(true);
        }
    }
}