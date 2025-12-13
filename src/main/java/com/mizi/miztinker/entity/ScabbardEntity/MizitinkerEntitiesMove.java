package com.mizi.miztinker.entity.ScabbardEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class MizitinkerEntitiesMove {
    public static Vec3 calculateMovementVector(float yaw, float pitch) {
        double yawRadians = Math.toRadians(-yaw);
        double pitchRadians = Math.toRadians(-pitch);
        double horizontalMagnitude = Math.cos(pitchRadians);

        double motionX = horizontalMagnitude * Math.sin(yawRadians);
        double motionY = Math.sin(pitchRadians);
        double motionZ = horizontalMagnitude * Math.cos(yawRadians);
        return new Vec3(motionX, motionY, motionZ);
    }

    public static boolean isValidTarget(LivingEntity living,Entity owner) {
        if (living == owner) {
            return false;
        }
        if (owner instanceof Player playerOwner) {
            if (living instanceof Player && ((Player) living).isCreative()) {
                return false;
            }
            if (playerOwner.isAlliedTo(living)) {
                return false;
            }
        }
        return living.isAlive();
    }
    private static final Map<Entity, Map<LivingEntity, Integer>> ignoredTargets = new HashMap<>();

    public static void moveTowardsTargetWithTransfer(Entity entity, LivingEntity target, double speed,boolean transferAdd, double transferDistance) {
        Vec3 currentPos = entity.position();
        Vec3 targetPos = target.getEyePosition();
        double distance = currentPos.distanceTo(targetPos);
        if (distance <= transferDistance) {
            if (transferAdd) {
                addToIgnoredTargets(entity, target, 10);
            }
            entity.setDeltaMovement(Vec3.ZERO);
            return;
        }

        Vec3 direction = targetPos.subtract(currentPos).normalize();
        entity.setDeltaMovement(direction.scale(speed));
    }

    public static LivingEntity findNearestTargetWithTransfer(Entity entity, Entity owner, double detectionRange) {
        List<LivingEntity> nearbyEntities = entity.level().getEntitiesOfClass(LivingEntity.class,
                entity.getBoundingBox().inflate(detectionRange));

        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity living : nearbyEntities) {
            if (isValidTarget(living, owner) && !isTargetIgnored(entity, living)) {
                double distance = entity.distanceToSqr(living);
                if (distance < nearestDistance) {
                    nearestTarget = living;
                    nearestDistance = distance;
                }
            }
        }
        return nearestTarget;
    }

    private static boolean isTargetIgnored(Entity entity, LivingEntity target) {
        Map<LivingEntity, Integer> entityIgnoredTargets = ignoredTargets.get(entity);
        if (entityIgnoredTargets == null) {
            return false;
        }

        Integer ignoreTicks = entityIgnoredTargets.get(target);
        if (ignoreTicks == null) {
            return false;
        }

        if (ignoreTicks <= 0) {
            entityIgnoredTargets.remove(target);
            if (entityIgnoredTargets.isEmpty()) {
                ignoredTargets.remove(entity);
            }
            return false;
        }

        return true;
    }

    private static void addToIgnoredTargets(Entity entity, LivingEntity target, int ignoreTicks) {
        Map<LivingEntity, Integer> entityIgnoredTargets = ignoredTargets.computeIfAbsent(entity, k -> new HashMap<>());
        entityIgnoredTargets.put(target, ignoreTicks);
    }

    public static boolean isInLoadedChunk(Entity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();

        if (!level.isLoaded(pos)) {
            return false;
        }
        AABB boundingBox = entity.getBoundingBox();
        BlockPos minPos = new BlockPos(
                (int) Math.floor(boundingBox.minX),
                (int) Math.floor(boundingBox.minY),
                (int) Math.floor(boundingBox.minZ)
        );
        BlockPos maxPos = new BlockPos(
                (int) Math.floor(boundingBox.maxX),
                (int) Math.floor(boundingBox.maxY),
                (int) Math.floor(boundingBox.maxZ)
        );

        return level.isLoaded(minPos) && level.isLoaded(maxPos);
    }

    public static void maintainRelativePosition(Entity entity, Entity owner, double offsetX, double offsetY, double offsetZ) {
        // 计算目标位置（相对于主人的固定偏移）
        double targetX = owner.getX() + offsetX;
        double targetY = owner.getY() + offsetY;
        double targetZ = owner.getZ() + offsetZ;

        // 计算当前位置与目标位置的差值
        double currentX = entity.getX();
        double currentY = entity.getY();
        double currentZ = entity.getZ();

        double diffX = targetX - currentX;
        double diffY = targetY - currentY;
        double diffZ = targetZ - currentZ;

        // 使用平滑因子移动
        double speedFactor = 0.6;
        double moveX = diffX * speedFactor;
        double moveY = diffY * speedFactor;
        double moveZ = diffZ * speedFactor;

        // 设置移动速度和位置
        entity.setDeltaMovement(moveX, moveY, moveZ);
        entity.setPos(
                currentX + moveX,
                currentY + moveY,
                currentZ + moveZ
        );
    }
}
