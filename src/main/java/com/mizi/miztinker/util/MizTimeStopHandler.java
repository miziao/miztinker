package com.mizi.miztinker.util;

import com.mizi.miztinker.mixins.MobAccessor;
import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.TimeStopPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MizTimeStopHandler {
    private static final String FROZEN_MARKER = "miztinker_time_stop_frozen";

    private static final Map<UUID, FrozenState> FROZEN_ENTITIES = new ConcurrentHashMap<>();

    private static UUID controller;

    private static Integer savedRandomTickSpeed = null;

    public static boolean toggle(Entity owner, boolean active) {
        MinecraftServer server = owner.getServer();

        if (server == null) {
            return false;
        }

        UUID ownerId = owner.getUUID();

        if (active) {
            if (controller != null) {
                return controller.equals(ownerId);
            }

            controller = ownerId;

            pauseRandomTicks(server);
            syncShader(server, true);
            freezeServer(server);

            return true;
        }

        if (controller == null) {
            return true;
        }

        if (!controller.equals(ownerId)) {
            return false;
        }

        resume(server, false);
        return true;
    }

    public static boolean isControllingTimeStop(Entity entity) {
        return entity != null && entity.getUUID().equals(controller);
    }

    public static boolean isTimeStopped() {
        return controller != null;
    }

    private static void resume(MinecraftServer server, boolean forced) {
        restoreFrozenEntities(server);
        restoreRandomTicks(server);

        controller = null;
        syncShader(server, false);

        if (forced) {
            server.getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.miztinker.timestop.force_resume")));
        }
    }

    private static void pauseRandomTicks(MinecraftServer server) {
        if (server == null) {
            return;
        }

        GameRules.IntegerValue randomTickRule = server.getGameRules().getRule(GameRules.RULE_RANDOMTICKING);

        if (savedRandomTickSpeed == null) {
            savedRandomTickSpeed = randomTickRule.get();
        }

        randomTickRule.set(0, server);
    }

    private static void restoreRandomTicks(MinecraftServer server) {
        if (server == null || savedRandomTickSpeed == null) {
            return;
        }

        server.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(savedRandomTickSpeed, server);
        savedRandomTickSpeed = null;
    }

    private static void freezeServer(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            freezeLevel(level);
        }
    }

    private static void freezeLevel(ServerLevel level) {
        for (Entity entity : level.getEntities().getAll()) {
            freezeEntity(entity);
        }

        for (ServerPlayer player : level.players()) {
            freezeEntity(player);
        }
    }

    private static void restoreFrozenEntities(MinecraftServer server) {
        for (Map.Entry<UUID, FrozenState> entry : FROZEN_ENTITIES.entrySet()) {
            FrozenState state = entry.getValue();

            ServerLevel level = server.getLevel(state.dimension);
            Entity entity = level == null ? null : level.getEntity(entry.getKey());

            if (entity == null) {
                entity = server.getPlayerList().getPlayer(entry.getKey());
            }

            if (entity != null) {
                state.restore(entity);
            }
        }

        FROZEN_ENTITIES.clear();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();

        if (server == null) {
            return;
        }

        if (event.getEntity().getUUID().equals(controller)) {
            resume(server, true);
        } else {
            restoreAndForget(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MiztinkerNetwork.sendToPlayer(new TimeStopPacket(isTimeStopped(), controller), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restoreAndForget(player);
            MiztinkerNetwork.sendToPlayer(new TimeStopPacket(isTimeStopped(), controller), player);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        freezeEntity(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && !isTimeStopped()) {
            clearTimeStopResidue(event.getEntity());
        } else if (!event.getLevel().isClientSide) {
            freezeEntity(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!isTimeStopped()) {
            if (event.phase == TickEvent.Phase.END && serverLevel.getGameTime() % 20L == 0L) {
                clearTimeStopResidue(serverLevel);
            }
            return;
        }

        if (event.phase == TickEvent.Phase.START || event.phase == TickEvent.Phase.END) {
            freezeLevel(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        restoreFrozenEntities(event.getServer());
        restoreRandomTicks(event.getServer());
        controller = null;
    }

    private static void freezeEntity(Entity entity) {
        if (!shouldFreeze(entity)) {
            return;
        }

        FrozenState state = FROZEN_ENTITIES.computeIfAbsent(
                entity.getUUID(),
                ignored -> FrozenState.capture(entity)
        );

        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }

        state.pin(entity);
    }

    private static boolean shouldFreeze(Entity entity) {
        if (entity == null || controller == null || entity.level().isClientSide || entity.isRemoved()) {
            return false;
        }

        if (entity.getUUID().equals(controller)) {
            return false;
        }

        return entity instanceof LivingEntity
                || entity instanceof Projectile
                || entity instanceof ItemEntity;
    }

    public static boolean isEntityStopped(Entity entity) {
        return shouldFreeze(entity);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (isEntityStopped(event.getSource().getEntity())
                || isEntityStopped(event.getSource().getDirectEntity())) {
            event.setCanceled(true);
        }
    }

    private static void restoreAndForget(Entity entity) {
        FrozenState state = FROZEN_ENTITIES.remove(entity.getUUID());

        if (state != null) {
            state.restore(entity);
        }
    }

    private static void syncShader(MinecraftServer server, boolean active) {
        TimeStopPacket packet = new TimeStopPacket(active, active ? controller : null);

        server.getPlayerList().getPlayers().forEach(player ->
                MiztinkerNetwork.sendToPlayer(packet, player)
        );
    }

    private static void clearTimeStopResidue(ServerLevel level) {
        for (Entity entity : level.getEntities().getAll()) {
            clearTimeStopResidue(entity);
        }

        for (ServerPlayer player : level.players()) {
            clearTimeStopResidue(player);
        }
    }

    private static void clearTimeStopResidue(Entity entity) {
        if (entity == null || entity.isRemoved()) {
            return;
        }

        boolean marked = entity.getPersistentData().getBoolean(FROZEN_MARKER);
        boolean wasNotUpdating = !entity.canUpdate();

        if (!marked && !wasNotUpdating) {
            return;
        }

        if (entity instanceof LivingEntity || entity instanceof Projectile || entity instanceof ItemEntity) {
            entity.canUpdate(true);
            entity.setNoGravity(false);
        }

        if (entity instanceof Mob mob && mob instanceof Enemy && (marked || wasNotUpdating || hasStoredNoAi(mob))) {
            mob.setNoAi(false);
        }

        entity.getPersistentData().remove(FROZEN_MARKER);
    }

    private static boolean hasStoredNoAi(Mob mob) {
        return (mob.getEntityData().get(MobAccessor.miztinker$getMobFlagsId()) & 1) != 0;
    }

    private static class FrozenState {
        private final ResourceKey<Level> dimension;
        private final Vec3 position;
        private final Vec3 deltaMovement;

        private final float yRot;
        private final float xRot;
        private final float yBodyRot;
        private final float yHeadRot;

        private final float fallDistance;
        private final boolean noGravity;
        private final boolean canUpdate;
        private final boolean mob;
        private final boolean noAi;
        private final int tickCount;

        private boolean stopMotionSynced;

        private FrozenState(
                ResourceKey<Level> dimension,
                Vec3 position,
                Vec3 deltaMovement,
                float yRot,
                float xRot,
                float yBodyRot,
                float yHeadRot,
                float fallDistance,
                boolean noGravity,
                boolean canUpdate,
                boolean mob,
                boolean noAi,
                int tickCount
        ) {
            this.dimension = dimension;
            this.position = position;
            this.deltaMovement = deltaMovement;
            this.yRot = yRot;
            this.xRot = xRot;
            this.yBodyRot = yBodyRot;
            this.yHeadRot = yHeadRot;
            this.fallDistance = fallDistance;
            this.noGravity = noGravity;
            this.canUpdate = canUpdate;
            this.mob = mob;
            this.noAi = noAi;
            this.tickCount = tickCount;
        }

        private static FrozenState capture(Entity entity) {
            entity.getPersistentData().putBoolean(FROZEN_MARKER, true);

            boolean isMob = entity instanceof Mob;
            boolean originalNoAi = isMob && hasStoredNoAi((Mob) entity) && !(entity instanceof Enemy);

            float originalBodyRot = entity instanceof LivingEntity living
                    ? living.yBodyRot
                    : entity.getYRot();

            float originalHeadRot = entity instanceof LivingEntity living
                    ? living.getYHeadRot()
                    : entity.getYRot();

            return new FrozenState(
                    entity.level().dimension(),
                    getFrozenPosition(entity),
                    entity.getDeltaMovement(),
                    entity.getYRot(),
                    entity.getXRot(),
                    originalBodyRot,
                    originalHeadRot,
                    entity.fallDistance,
                    entity.isNoGravity(),
                    entity.canUpdate(),
                    isMob,
                    originalNoAi,
                    entity.tickCount
            );
        }

        private static Vec3 getFrozenPosition(Entity entity) {
            if (entity instanceof Projectile projectile && projectile.tickCount <= 1) {
                Vec3 motion = projectile.getDeltaMovement();

                if (motion.lengthSqr() > 1.0E-7D) {
                    return projectile.position().add(motion.normalize().scale(1.6D));
                }
            }

            return entity.position();
        }

        private void pin(Entity entity) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setNoGravity(true);
            entity.fallDistance = 0.0F;

            if (!stopMotionSynced) {
                entity.hurtMarked = true;
                entity.hasImpulse = true;
                stopMotionSynced = true;
            }

            entity.tickCount = tickCount;

            if (entity instanceof ServerPlayer player) {
                player.connection.teleport(position.x, position.y, position.z, yRot, xRot);
            } else {
                entity.setPos(position.x, position.y, position.z);
            }

            entity.setYRot(yRot);
            entity.setXRot(xRot);

            entity.xo = position.x;
            entity.yo = position.y;
            entity.zo = position.z;

            entity.yRotO = yRot;
            entity.xRotO = xRot;

            if (entity instanceof LivingEntity living) {
                living.setYBodyRot(yBodyRot);
                living.yBodyRotO = yBodyRot;

                living.setYHeadRot(yHeadRot);
                living.yHeadRotO = yHeadRot;

                living.walkAnimation.setSpeed(0.0F);
                living.setSpeed(0.0F);
                living.setLastHurtByMob(null);
            }
        }

        private void restore(Entity entity) {
            entity.setNoGravity(noGravity);
            entity.canUpdate(canUpdate);
            entity.setDeltaMovement(deltaMovement);
            entity.fallDistance = fallDistance;

            entity.hurtMarked = true;
            entity.hasImpulse = true;

            if (mob && entity instanceof Mob restoredMob) {
                restoredMob.setNoAi(noAi && !(restoredMob instanceof Enemy));
            }

            if (entity instanceof LivingEntity living) {
                living.setYBodyRot(yBodyRot);
                living.yBodyRotO = yBodyRot;

                living.setYHeadRot(yHeadRot);
                living.yHeadRotO = yHeadRot;
            }

            if (entity instanceof ServerPlayer player) {
                player.connection.teleport(position.x, position.y, position.z, yRot, xRot);
            } else if (entity.level().isLoaded(BlockPos.containing(position))) {
                entity.setPos(position.x, position.y, position.z);
            }

            entity.getPersistentData().remove(FROZEN_MARKER);
        }
    }
}