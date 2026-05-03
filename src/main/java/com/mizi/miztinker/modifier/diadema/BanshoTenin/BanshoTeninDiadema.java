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

        List<Entity> entities = level.getEntities(owner, area, e -> e.isAlive() && !e.isSpectator());

        for (Entity entity : entities) {

            if (entity.is(owner) || entity.isAlliedTo(owner) || owner.getVehicle() == entity || entity.getVehicle() == owner) {
                continue;
            }

            Vec3 targetVector = center.subtract(entity.position());
            double distance = targetVector.length();

            if (distance > 1.0) {
                Vec3 motion = targetVector.normalize().scale(ATTRACT_SPEED);
                entity.setDeltaMovement(entity.getDeltaMovement().add(motion));
                entity.hurtMarked = true;
            }
        }
        }
    }
