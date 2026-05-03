package com.mizi.miztinker.modifier.diadema.paper_bomb;

import com.csdy.tcondiadema.frames.diadema.ClientDiadema;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class TandemPaperBombClientDiadema extends ClientDiadema {
    private static final double RADIUS = 6.0;
    private static final double HEIGHT = 4.0;

    private static final net.minecraft.core.particles.SimpleParticleType PARTICLE_EXPLOSION = ParticleTypes.EXPLOSION;
    private static final net.minecraft.core.particles.SimpleParticleType PARTICLE_FLAME = ParticleTypes.FLAME;

    private final Random random = new Random();

    @Override
    protected void perTick() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        if (!level.dimension().location().equals(getDimension())) return;

        for (int i = 0; i < 3; i++) {
            drawRandomParticle(level);
        }
    }

    private void drawRandomParticle(Level level) {
        Vec3 entityPosition = getPosition();

        double r = random.nextDouble() * RADIUS;
        double theta = random.nextDouble() * 2.0 * Math.PI;
        double h = random.nextDouble() * HEIGHT;

        double x = r * Math.cos(theta);
        double z = r * Math.sin(theta);

        double spawnX = entityPosition.x() + x;
        double spawnY = entityPosition.y() + h;
        double spawnZ = entityPosition.z() + z;

        if (random.nextBoolean()) {
            level.addParticle(PARTICLE_EXPLOSION, spawnX, spawnY, spawnZ, 0, 0, 0);
        } else {
            level.addParticle(PARTICLE_FLAME, spawnX, spawnY, spawnZ, 0, 0.02, 0);
        }
    }
}