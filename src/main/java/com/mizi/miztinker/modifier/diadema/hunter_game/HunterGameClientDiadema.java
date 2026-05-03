package com.mizi.miztinker.modifier.diadema.hunter_game;

import com.csdy.tcondiadema.frames.diadema.ClientDiadema;
import com.csdy.tcondiadema.particleUtils.PointSets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HunterGameClientDiadema extends ClientDiadema {
    private static final double VISUAL_RADIUS = 15.0;
    private static final int segX = 16;
    private static final int segY = 16;

    @Override
    protected void perTick() {
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(getDimension())) return;

        drawField(level);
    }

    private void drawField(Level level) {
        Vec3 center = getPosition();
        PointSets.Sphere(VISUAL_RADIUS, segX, segY).forEach(v -> {
            double px = center.x + v.x;
            double py = center.y + v.y;
            double pz = center.z + v.z;

            level.addParticle(ParticleTypes.INSTANT_EFFECT, px, py, pz, 0, 0, 0);
        });
    }
}