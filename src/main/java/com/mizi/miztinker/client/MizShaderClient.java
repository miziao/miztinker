package com.mizi.miztinker.client;

import com.mizi.miztinker.mixins.PostChainAccessor;
import com.mizi.miztinker.mixins.SoundEngineAccessor;
import com.mizi.miztinker.mixins.SoundManagerAccessor;
import com.mizi.miztinker.mixins.WalkAnimationStateAccessor;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "miztinker", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MizShaderClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MizShaderClient.class);

    private static final ResourceLocation POST_CHAIN_LOCATION =
            ResourceLocation.fromNamespaceAndPath("miztinker", "shaders/post/timestop_gray.json");

    private static final ResourceLocation SAKUYA_SOUND =
            ResourceLocation.fromNamespaceAndPath("miztinker", "sakuya");

    private static final long SPREAD_DURATION_MS = 950L;
    private static final Map<UUID, ClientFrozenState> FROZEN_ENTITIES = new HashMap<>();

    private static final Set<SoundInstance> PAUSED_SOUNDS_BY_TIME_STOP =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private static PostChain postChain;
    private static boolean timeStopActive = false;
    private static boolean shaderVisible = false;
    private static boolean closingShader = false;
    private static boolean postChainLoadFailed = false;
    private static boolean soundPausedByTimeStop = false;
    private static long shaderStartTimeMs = 0L;
    private static UUID controllerId;

    public static void setShaderActive(boolean active) {
        setTimeStopActive(active, null);
    }

    public static void setTimeStopActive(boolean active, UUID controller) {
        boolean starting = active && !timeStopActive;
        boolean stopping = !active && (timeStopActive || shaderVisible);

        timeStopActive = active;
        controllerId = active ? controller : null;

        if (starting) {
            shaderVisible = true;
            closingShader = false;
            shaderStartTimeMs = System.currentTimeMillis();
        } else if (stopping) {
            shaderVisible = true;
            closingShader = true;
            shaderStartTimeMs = System.currentTimeMillis();
        }

        Minecraft mc = Minecraft.getInstance();
        mc.tell(() -> {
            if (active) {
                postChainLoadFailed = false;
                ensurePostChain(mc);

                setTimeStopSoundPaused(mc, true);
            } else {
                setTimeStopSoundPaused(mc, false);

                releaseFrozenEntities();

                if (shaderVisible) {
                    ensurePostChain(mc);
                } else {
                    closePostChain();
                }
            }
        });
    }

    public static boolean isEntityVisuallyStopped(Entity entity) {
        if (!timeStopActive || entity == null || entity.isRemoved()) {
            return false;
        }

        return controllerId == null || !controllerId.equals(entity.getUUID());
    }

    public static boolean isTimeStopVisualActive() {
        return timeStopActive;
    }

    public static boolean shouldBlockNewTimeStopSounds(SoundInstance sound) {
        if (!timeStopActive) {
            return false;
        }

        return !isAllowedTimeStopSound(sound);
    }

    private static boolean isAllowedTimeStopSound(SoundInstance sound) {
        if (sound == null || sound.getLocation() == null) {
            return false;
        }

        return SAKUYA_SOUND.equals(sound.getLocation());
    }

    private static void setTimeStopSoundPaused(Minecraft mc, boolean paused) {
        if (mc == null || mc.getSoundManager() == null) {
            return;
        }

        if (paused) {
            if (!soundPausedByTimeStop) {
                soundPausedByTimeStop = true;
                pauseCurrentSoundsExceptSakuya(mc);
            }
            return;
        }

        if (soundPausedByTimeStop) {
            soundPausedByTimeStop = false;
            resumePausedTimeStopSounds(mc);
        }
    }

    private static void pauseCurrentSoundsExceptSakuya(Minecraft mc) {
        SoundEngine engine = getSoundEngine(mc);

        if (engine == null) {
            return;
        }

        Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel =
                ((SoundEngineAccessor) engine).miztinker$getInstanceToChannel();

        for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : new ArrayList<>(instanceToChannel.entrySet())) {
            SoundInstance sound = entry.getKey();
            ChannelAccess.ChannelHandle handle = entry.getValue();

            if (sound == null || handle == null) {
                continue;
            }

            if (isAllowedTimeStopSound(sound)) {
                continue;
            }

            handle.execute(Channel::pause);
            PAUSED_SOUNDS_BY_TIME_STOP.add(sound);
        }
    }

    private static void resumePausedTimeStopSounds(Minecraft mc) {
        SoundEngine engine = getSoundEngine(mc);

        if (engine == null) {
            PAUSED_SOUNDS_BY_TIME_STOP.clear();
            return;
        }

        Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel =
                ((SoundEngineAccessor) engine).miztinker$getInstanceToChannel();

        for (SoundInstance sound : new ArrayList<>(PAUSED_SOUNDS_BY_TIME_STOP)) {
            ChannelAccess.ChannelHandle handle = instanceToChannel.get(sound);

            if (handle != null) {
                handle.execute(Channel::unpause);
            }

            PAUSED_SOUNDS_BY_TIME_STOP.remove(sound);
        }
    }

    private static SoundEngine getSoundEngine(Minecraft mc) {
        try {
            return ((SoundManagerAccessor) mc.getSoundManager()).miztinker$getSoundEngine();
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to access SoundEngine for time stop sound control", e);
            return null;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (!timeStopActive) {
            setTimeStopSoundPaused(mc, false);

            if (!FROZEN_ENTITIES.isEmpty()) {
                releaseFrozenEntities();
            }
            return;
        }

        freezeVisualEntities(mc);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && shouldFreezeVisual(event.getEntity())) {
            freezeVisualEntity(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        Entity entity = event.getEntity();

        if (shouldFreezeVisual(entity)) {
            freezeVisualEntity(entity);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY && timeStopActive) {
            freezeVisualEntities(Minecraft.getInstance());
            return;
        }

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || !shaderVisible) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ensurePostChain(mc);

        if (postChain == null) {
            if (closingShader) {
                finishClosingShader();
            }
            return;
        }

        postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());

        float spread = getSpreadProgress();
        updateSpreadUniform(spread);

        postChain.process(event.getPartialTick());
        mc.getMainRenderTarget().bindWrite(true);

        if (closingShader && spread <= 0.0F) {
            finishClosingShader();
        }
    }

    private static float getSpreadProgress() {
        if (shaderStartTimeMs == 0L) {
            return closingShader ? 0.0F : 1.0F;
        }

        float progress = (System.currentTimeMillis() - shaderStartTimeMs) / (float) SPREAD_DURATION_MS;
        progress = Math.min(1.0F, Math.max(0.0F, progress));

        return closingShader ? 1.0F - progress : progress;
    }

    private static void updateSpreadUniform(float spread) {
        List<PostPass> passes = ((PostChainAccessor) postChain).miztinker$getPasses();

        if (!passes.isEmpty()) {
            passes.get(0).getEffect().safeGetUniform("Spread").set(spread);
        }
    }

    private static void finishClosingShader() {
        shaderVisible = false;
        closingShader = false;
        shaderStartTimeMs = 0L;
        closePostChain();
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        resetClientState();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        resetClientState();
    }

    private static void resetClientState() {
        Minecraft mc = Minecraft.getInstance();

        setTimeStopSoundPaused(mc, false);

        timeStopActive = false;
        shaderVisible = false;
        closingShader = false;
        controllerId = null;
        postChainLoadFailed = false;
        shaderStartTimeMs = 0L;

        releaseFrozenEntities();
        closePostChain();
    }

    private static void ensurePostChain(Minecraft mc) {
        if (postChain != null || postChainLoadFailed || mc.getMainRenderTarget() == null) {
            return;
        }

        try {
            postChain = new PostChain(
                    mc.getTextureManager(),
                    mc.getResourceManager(),
                    mc.getMainRenderTarget(),
                    POST_CHAIN_LOCATION
            );
            postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        } catch (IOException | RuntimeException e) {
            postChain = null;
            postChainLoadFailed = true;
            LOGGER.warn("Failed to load time stop post shader {}", POST_CHAIN_LOCATION, e);
        }
    }

    private static void closePostChain() {
        if (postChain != null) {
            postChain.close();
            postChain = null;
        }
    }

    private static void freezeVisualEntities(Minecraft mc) {
        if (mc.level == null) {
            releaseFrozenEntities();
            return;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (shouldFreezeVisual(entity)) {
                freezeVisualEntity(entity);
            }
        }
    }

    private static void freezeVisualEntity(Entity entity) {
        ClientFrozenState state = FROZEN_ENTITIES.computeIfAbsent(
                entity.getUUID(),
                ignored -> ClientFrozenState.capture(entity)
        );

        state.pin(entity);
    }

    private static boolean shouldFreezeVisual(Entity entity) {
        if (!isEntityVisuallyStopped(entity)) {
            return false;
        }

        return entity instanceof LivingEntity
                || entity instanceof Projectile
                || entity instanceof ItemEntity;
    }

    private static void releaseFrozenEntities() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            for (ClientFrozenState state : FROZEN_ENTITIES.values()) {
                Entity entity = mc.level.getEntity(state.entityId);

                if (entity != null) {
                    state.restore(entity);
                }
            }
        }

        FROZEN_ENTITIES.clear();
    }

    private static class ClientFrozenState {
        private final int entityId;

        private final Vec3 position;
        private final Vec3 deltaMovement;

        private final float yRot;
        private final float xRot;
        private final float yBodyRot;
        private final float yHeadRot;

        private final boolean canUpdate;
        private final boolean noGravity;
        private final int tickCount;

        private final float attackAnim;
        private final float oAttackAnim;

        private final int hurtTime;
        private final int hurtDuration;
        private final int deathTime;

        private final boolean swinging;
        private final int swingTime;

        private final float walkSpeed;
        private final float walkSpeedOld;
        private final float walkPosition;

        private ClientFrozenState(
                int entityId,
                Vec3 position,
                Vec3 deltaMovement,
                float yRot,
                float xRot,
                float yBodyRot,
                float yHeadRot,
                boolean canUpdate,
                boolean noGravity,
                int tickCount,
                float attackAnim,
                float oAttackAnim,
                int hurtTime,
                int hurtDuration,
                int deathTime,
                boolean swinging,
                int swingTime,
                float walkSpeed,
                float walkSpeedOld,
                float walkPosition
        ) {
            this.entityId = entityId;
            this.position = position;
            this.deltaMovement = deltaMovement;
            this.yRot = yRot;
            this.xRot = xRot;
            this.yBodyRot = yBodyRot;
            this.yHeadRot = yHeadRot;
            this.canUpdate = canUpdate;
            this.noGravity = noGravity;
            this.tickCount = tickCount;
            this.attackAnim = attackAnim;
            this.oAttackAnim = oAttackAnim;
            this.hurtTime = hurtTime;
            this.hurtDuration = hurtDuration;
            this.deathTime = deathTime;
            this.swinging = swinging;
            this.swingTime = swingTime;
            this.walkSpeed = walkSpeed;
            this.walkSpeedOld = walkSpeedOld;
            this.walkPosition = walkPosition;
        }

        private static ClientFrozenState capture(Entity entity) {
            float originalBodyRot = entity instanceof LivingEntity living
                    ? living.yBodyRot
                    : entity.getYRot();

            float originalHeadRot = entity instanceof LivingEntity living
                    ? living.getYHeadRot()
                    : entity.getYRot();

            float attackAnim = 0.0F;
            float oAttackAnim = 0.0F;

            int hurtTime = 0;
            int hurtDuration = 0;
            int deathTime = 0;

            boolean swinging = false;
            int swingTime = 0;

            float walkSpeed = 0.0F;
            float walkSpeedOld = 0.0F;
            float walkPosition = 0.0F;

            if (entity instanceof LivingEntity living) {
                attackAnim = living.attackAnim;
                oAttackAnim = living.oAttackAnim;

                hurtTime = living.hurtTime;
                hurtDuration = living.hurtDuration;
                deathTime = living.deathTime;

                swinging = living.swinging;
                swingTime = living.swingTime;

                WalkAnimationStateAccessor walk = (WalkAnimationStateAccessor) living.walkAnimation;
                walkSpeed = walk.miztinker$getSpeed();
                walkSpeedOld = walk.miztinker$getSpeedOld();
                walkPosition = walk.miztinker$getPosition();
            }

            return new ClientFrozenState(
                    entity.getId(),
                    entity.position(),
                    entity.getDeltaMovement(),
                    entity.getYRot(),
                    entity.getXRot(),
                    originalBodyRot,
                    originalHeadRot,
                    entity.canUpdate(),
                    entity.isNoGravity(),
                    entity.tickCount,
                    attackAnim,
                    oAttackAnim,
                    hurtTime,
                    hurtDuration,
                    deathTime,
                    swinging,
                    swingTime,
                    walkSpeed,
                    walkSpeedOld,
                    walkPosition
            );
        }

        private void pin(Entity entity) {
            entity.canUpdate(false);

            entity.setDeltaMovement(Vec3.ZERO);
            entity.setNoGravity(true);

            if (entity instanceof Projectile) {
                entity.tickCount = Math.max(tickCount, 2);
            } else {
                entity.tickCount = tickCount;
            }

            entity.setPos(position.x, position.y, position.z);

            entity.xo = position.x;
            entity.yo = position.y;
            entity.zo = position.z;

            entity.xOld = position.x;
            entity.yOld = position.y;
            entity.zOld = position.z;

            entity.setYRot(yRot);
            entity.setXRot(xRot);

            entity.yRotO = yRot;
            entity.xRotO = xRot;

            if (entity instanceof LivingEntity living) {
                living.setYBodyRot(yBodyRot);
                living.yBodyRotO = yBodyRot;

                living.setYHeadRot(yHeadRot);
                living.yHeadRotO = yHeadRot;

                living.attackAnim = attackAnim;
                living.oAttackAnim = oAttackAnim;

                living.hurtTime = hurtTime;
                living.hurtDuration = hurtDuration;
                living.deathTime = deathTime;

                living.swinging = swinging;
                living.swingTime = swingTime;

                WalkAnimationStateAccessor walk = (WalkAnimationStateAccessor) living.walkAnimation;
                walk.miztinker$setSpeed(walkSpeed);
                walk.miztinker$setSpeedOld(walkSpeedOld);
                walk.miztinker$setPosition(walkPosition);
            }
        }

        private void restore(Entity entity) {
            entity.canUpdate(canUpdate);
            entity.setNoGravity(noGravity);
            entity.setDeltaMovement(deltaMovement);
        }
    }
}