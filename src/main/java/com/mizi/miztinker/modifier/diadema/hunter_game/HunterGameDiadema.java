package com.mizi.miztinker.modifier.diadema.hunter_game;

import com.csdy.tcondiadema.frames.diadema.Diadema;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.diadema.movement.DiademaMovement;
import com.csdy.tcondiadema.frames.diadema.range.DiademaRange;
import com.csdy.tcondiadema.diadema.api.ranges.SphereDiademaRange;
import lombok.NonNull;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.UUID;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.modifierCutting;

public class HunterGameDiadema extends Diadema {
    private static final double RADIUS = 15.0;
    private int tickCounter = 0;
    private UUID lockedTargetUUID = null;

    private static final Capability<Object> DIADEMA_HOLDER_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    public HunterGameDiadema(DiademaType type, DiademaMovement movement) {
        super(type, movement);
    }

    private final SphereDiademaRange range = new SphereDiademaRange(this, RADIUS);

    @Override
    public @NonNull DiademaRange getRange() {
        return range;
    }

    @Override
    protected void perTick() {
        Level level = getLevel();
        if (level.isClientSide) return;

        Entity owner = getCoreEntity();

        if (!(owner instanceof LivingEntity livingOwner) || !owner.isAlive()) {
            this.kill();
            return;
        }

        tickCounter++;
        if (tickCounter % 2 != 0) return;

        Vec3 center = getPosition();
        AABB area = new AABB(center.x - RADIUS, center.y - RADIUS, center.z - RADIUS,
                center.x + RADIUS, center.y + RADIUS, center.z + RADIUS);

        LivingEntity target = validateAndGetTarget(level, livingOwner, area);

        if (target != null) {
            shootHunterArrow(level, livingOwner, target, center);
        }
    }

    private LivingEntity validateAndGetTarget(Level level, LivingEntity owner, AABB area) {
        UUID ownerUUID = owner.getUUID();

        if (lockedTargetUUID != null) {
            Entity entity = ((net.minecraft.server.level.ServerLevel)level).getEntity(lockedTargetUUID);
            if (entity instanceof LivingEntity living && living.isAlive() && area.contains(living.position())) {
                if (!living.getUUID().equals(ownerUUID) && isLockable(living)) {
                    return living;
                }
            }
            lockedTargetUUID = null;
        }

        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive()
                        && !e.isSpectator()
                        && e != owner
                        && !e.getUUID().equals(ownerUUID)
                        && !e.isAlliedTo(owner)
                        && isLockable(e));

        if (!potentialTargets.isEmpty()) {
            LivingEntity newTarget = potentialTargets.get(0);
            lockedTargetUUID = newTarget.getUUID();
            return newTarget;
        }

        return null;
    }

    private boolean isLockable(LivingEntity entity) {
        LazyOptional<Object> cap = entity.getCapability(DIADEMA_HOLDER_CAP);
        return cap.map(holder -> {
            try {
                java.lang.reflect.Method getInstance = holder.getClass().getMethod("getInstance");
                Object instance = getInstance.invoke(holder);
                return instance == null;
            } catch (Exception e) {
                return true;
            }
        }).orElse(true);
    }

    private static class CleanArrow extends Arrow {
        public CleanArrow(Level level, double x, double y, double z) {
            super(EntityType.ARROW, level);
            this.setPos(x, y, z);
        }

        @Override
        public void defineSynchedData() {
            super.defineSynchedData();
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.level().isClientSide && (this.inGround || this.tickCount > 20)) {
                this.discard();
            }
        }
    }

    private void shootHunterArrow(Level level, LivingEntity owner, LivingEntity target, Vec3 center) {
        Vec3 spawnPos = center.add(0, RADIUS, 0);

        CleanArrow arrow = new CleanArrow(level, spawnPos.x, spawnPos.y, spawnPos.z);
        arrow.setOwner(owner);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 direction = targetPos.subtract(spawnPos);

        arrow.shoot(direction.x, direction.y, direction.z, 3.0F, 0.0F);
        level.addFreshEntity(arrow);

        if (owner instanceof Player player) {
            modifierCutting(target, player, 1.0f, 0.02f);
        } else {
            applyNonPlayerCutting(target, owner);
        }
    }

    private void applyNonPlayerCutting(LivingEntity target, LivingEntity owner) {
        if (target.getHealth() <= 0) return;
        var mobKill = target.level().damageSources().mobAttack(owner);
        target.hurt(mobKill, 1);

        float reHealth = target.getHealth() - (0.02f) - target.getMaxHealth() * 0.01f;
        target.setHealth(reHealth);

        if (reHealth <= 0 || target.getHealth() <= 0) {
            target.die(mobKill);
        }
    }
}