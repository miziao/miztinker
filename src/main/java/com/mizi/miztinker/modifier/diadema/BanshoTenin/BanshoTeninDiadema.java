package com.mizi.miztinker.modifier.diadema.BanshoTenin;

import com.csdy.tcondiadema.frames.diadema.Diadema;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.diadema.movement.DiademaMovement;
import com.csdy.tcondiadema.frames.diadema.range.DiademaRange;
import com.csdy.tcondiadema.diadema.api.ranges.SphereDiademaRange;
import lombok.NonNull;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BanshoTeninDiadema extends Diadema {
    private static final double RADIUS = 64.0;
    private static final double ATTRACT_SPEED = 1.0;

    public BanshoTeninDiadema(DiademaType type, DiademaMovement movement) {
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
        if (owner == null || !owner.isAlive()) return;

        Vec3 center = getPosition();
        AABB area = new AABB(center.x - RADIUS, center.y - RADIUS, center.z - RADIUS,
                center.x + RADIUS, center.y + RADIUS, center.z + RADIUS);

        List<Entity> entities = level.getEntitiesOfClass(Entity.class, area, e -> e.isAlive() && !e.isSpectator());

        double SAFE_ZONE = 1.0;

        for (Entity entity : entities) {
            if (entity.is(owner) || entity.isAlliedTo(owner) || owner.getVehicle() == entity || entity.getVehicle() == owner) {
                continue;
            }

            Vec3 entityPos = entity.position();
            Vec3 targetVector = center.subtract(entityPos);
            double distance = targetVector.length();

            if (distance > SAFE_ZONE) {
                double strength = Math.min(ATTRACT_SPEED, Math.max(0.1, distance * 0.15));
                Vec3 attractMotion = targetVector.normalize().scale(strength);

                Vec3 currentMotion = entity.getDeltaMovement();

                double nextX = currentMotion.x * 0.6 + attractMotion.x * 0.15;
                double nextY = currentMotion.y * 0.6 + attractMotion.y * 0.15;
                double nextZ = currentMotion.z * 0.6 + attractMotion.z * 0.15;

                Vec3 finalMotion = new Vec3(nextX, nextY, nextZ);
                entity.setDeltaMovement(finalMotion);

                entity.hasImpulse = true;
                entity.hurtMarked = true;
            } else {
                entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5));
            }
        }
    }
    }

