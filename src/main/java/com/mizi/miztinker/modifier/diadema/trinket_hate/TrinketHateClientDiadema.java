package com.mizi.miztinker.modifier.diadema.trinket_hate;

import com.csdy.tcondiadema.frames.diadema.ClientDiadema;
import com.csdy.tcondiadema.particleUtils.PointSets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrinketHateClientDiadema extends ClientDiadema {
    private static final double RADIUS = 8.0;
    private static final double HEIGHT = 4.0;
    private static final net.minecraft.core.particles.SimpleParticleType PARTICLE_TYPE = ParticleTypes.ANGRY_VILLAGER;

    private static final int POINTS = 10;

    @Override
    protected void perTick() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        if (!level.dimension().location().equals(getDimension())) return;

        if (level.getGameTime() % 2 == 0) {
            drawParticle(level);
        }
    }

    private void drawParticle(Level level) {
        Vec3 entityPosition = getPosition();
        double yOffset = 0.1;

        double angle = (level.getGameTime() * 0.05);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        PointSets.Circle(RADIUS, POINTS)
                .map(p -> rotateY(p, cos, sin)) // 应用旋转
                .map(entityPosition::add)
                .forEach(pos -> level.addParticle(PARTICLE_TYPE, pos.x(), pos.y() + yOffset, pos.z(), 0, 0, 0));

        PointSets.Circle(RADIUS, POINTS)
                .map(p -> new Vec3(p.x(), p.z() * (HEIGHT / RADIUS), 0))
                .filter(p -> p.y >= 0)
                .map(p -> rotateY(p, cos, sin))
                .map(entityPosition::add)
                .forEach(pos -> level.addParticle(PARTICLE_TYPE, pos.x(), pos.y() + yOffset, pos.z(), 0, 0, 0));

        PointSets.Circle(RADIUS, POINTS)
                .map(p -> new Vec3(0, p.x() * (HEIGHT / RADIUS), p.z()))
                .filter(p -> p.y >= 0)
                .map(p -> rotateY(p, cos, sin))
                .map(entityPosition::add)
                .forEach(pos -> level.addParticle(PARTICLE_TYPE, pos.x(), pos.y() + yOffset, pos.z(), 0, 0, 0));
    }

    private Vec3 rotateY(Vec3 p, double cos, double sin) {
        double x = p.x * cos + p.z * sin;
        double z = -p.x * sin + p.z * cos;
        return new Vec3(x, p.y, z);
    }
}