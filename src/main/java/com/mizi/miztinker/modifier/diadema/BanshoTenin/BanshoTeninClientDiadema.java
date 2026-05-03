package com.mizi.miztinker.modifier.diadema.BanshoTenin;

import com.csdy.tcondiadema.frames.diadema.ClientDiadema;
import com.csdy.tcondiadema.particle.register.ParticlesRegister;
import com.csdy.tcondiadema.particleUtils.PointSets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BanshoTeninClientDiadema extends ClientDiadema {
    private static final SimpleParticleType type = ParticlesRegister.SHINRATENSEI_PARTICLE.get();
    private static final double VISUAL_RADIUS = 12.0;
    private static final int segX = 12;
    private static final int segY = 12;

    @Override
    protected void perTick() {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        if (!level.dimension().location().equals(getDimension())) return;

        drawInwardParticles(level);
    }

    private void drawInwardParticles(Level level) {
        Vec3 center = getPosition();

        PointSets.Sphere(VISUAL_RADIUS, segX, segY).forEach(v -> {
            var from = v.add(center);
            var target = v.scale(-0.15);

            level.addParticle(type, from.x, from.y, from.z, target.x, target.y, target.z);
        });
    }
}