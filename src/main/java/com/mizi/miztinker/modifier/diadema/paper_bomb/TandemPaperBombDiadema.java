package com.mizi.miztinker.modifier.diadema.paper_bomb;

import com.csdy.tcondiadema.frames.diadema.Diadema;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.diadema.movement.DiademaMovement;
import com.csdy.tcondiadema.frames.diadema.range.DiademaRange;
import com.csdy.tcondiadema.diadema.api.ranges.SphereDiademaRange;
import lombok.NonNull;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class TandemPaperBombDiadema extends Diadema {

    private static final double RADIUS = 6.0;
    private static final double HEIGHT = 4.0;
    private static final float EXPLOSION_POWER = 4.0F;
    private final Random random = new Random();

    public TandemPaperBombDiadema(DiademaType type, DiademaMovement movement) {
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

        for (int i = 0; i < 2; i++) {
            detonateRandomPaperBomb(level, owner);
        }
    }

    private void detonateRandomPaperBomb(Level level, Entity owner) {
        Vec3 center = getPosition();

        double r = random.nextDouble() * RADIUS;
        double theta = random.nextDouble() * 2.0 * Math.PI;
        double h = random.nextDouble() * HEIGHT;

        double x = center.x() + (r * Math.cos(theta));
        double y = center.y() + h;
        double z = center.z() + (r * Math.sin(theta));

        level.explode(
                owner,
                x, y, z,
                EXPLOSION_POWER,
                false,
                Level.ExplosionInteraction.BLOCK
        );
    }
}