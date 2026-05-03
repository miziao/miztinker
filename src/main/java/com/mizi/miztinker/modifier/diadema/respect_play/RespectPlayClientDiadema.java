package com.mizi.miztinker.modifier.diadema.respect_play;

import com.csdy.tcondiadema.frames.diadema.ClientDiadema;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RespectPlayClientDiadema extends ClientDiadema {
    private static final double HOR_R = 2.5;
    private static final double VER_R = 1.5;
    private static final double STEP = 0.5;

    @Override
    protected void perTick() {
        Level level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(getDimension())) return;

        drawCubeFrame(level);
    }

    private void drawCubeFrame(Level level) {
        Vec3 center = getPosition();
        double minX = center.x - HOR_R, maxX = center.x + HOR_R;
        double minZ = center.z - HOR_R, maxZ = center.z + HOR_R;
        double minY = center.y - VER_R, maxY = center.y + VER_R;

        for (double i = 0; i <= 5.0; i += STEP) {
            spawn(level, minX + i, minY, minZ);
            spawn(level, minX + i, minY, maxZ);
            spawn(level, minX, minY, minZ + i);
            spawn(level, maxX, minY, minZ + i);

            spawn(level, minX + i, maxY, minZ);
            spawn(level, minX + i, maxY, maxZ);
            spawn(level, minX, maxY, minZ + i);
            spawn(level, maxX, maxY, minZ + i);
        }

        for (double j = 0; j <= 3.0; j += STEP) {
            spawn(level, minX, minY + j, minZ);
            spawn(level, maxX, minY + j, minZ);
            spawn(level, minX, minY + j, maxZ);
            spawn(level, maxX, minY + j, maxZ);
        }
    }

    private void spawn(Level level, double x, double y, double z) {
        level.addParticle(ParticleTypes.CRIT, x, y, z, 0, 0, 0);
    }
}