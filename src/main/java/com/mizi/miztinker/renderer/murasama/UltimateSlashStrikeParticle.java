package com.mizi.miztinker.renderer.murasama;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.minecraft.util.Mth.nextFloat;

public class UltimateSlashStrikeParticle extends NoRenderParticle {
    // 可配置参数
    private final int particlesPerTick = 8;        // 每tick生成的轨迹粒子数量
    private final float startRadiusMin = 5f;      // 轨迹起始点最小半径（效果内圈半径）
    private final float startRadiusMax = 20f;      // 轨迹起始点最大半径（效果外圈半径）
    private final float trailLength = 40f + random.nextFloat()*10f;         // 轨迹长度（起始点到结束点的距离）
    private final float verticalAngleMin = 45f;   // 垂直角度最小值（控制效果的下边界）
    private final float verticalAngleMax = 80f;   // 垂直角度最大值（控制效果的上边界）
    private final float horizontalSpread = 45f;   // 水平扩散角度（轨迹的水平弯曲程度，值越大越分散）
    private final float verticalSpread = 20f;     // 垂直扩散角度（轨迹的垂直弯曲程度，值越大越分散）
    private final float globalScale = 2.5f;        // 全局缩放因子（整体效果尺寸缩放）

    public UltimateSlashStrikeParticle(ClientLevel level, double x, double y, double z, double rx, double ry, double rz) {
        super(level, x, y, z, rx, ry, rz);
        lifetime = 14;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        for (int i = 0; i < particlesPerTick; i++){
            float r = nextFloat(random, startRadiusMin, startRadiusMax);
            float theta = nextFloat(random, 0, 360);
            float beta = nextFloat(random, verticalAngleMin, verticalAngleMax);

            float r2 = r + trailLength;
            float theta2 = nextFloat(random, 180+theta-horizontalSpread, 180+theta+horizontalSpread);
            float beta2 = nextFloat(random, 180+beta-verticalSpread, 180+beta+verticalSpread);

            // 角度转弧度
            theta = (float) (theta/180*Math.PI);
            beta = (float) (beta/180*Math.PI);
            theta2 = (float) (theta2/180*Math.PI);
            beta2 = (float) (beta2/180*Math.PI);

            // 坐标计算
            double sr = r*Math.sin(beta);
            double sx = sr*Math.sin(theta)*globalScale;
            double sy = r*Math.cos(beta)*globalScale;
            double sz = sr*Math.cos(theta)*globalScale;

            double er = r2*Math.sin(beta2);
            double ex = er*Math.sin(theta2)*globalScale;
            double ey = r2*Math.cos(beta2)*globalScale;
            double ez = er*Math.cos(theta2)*globalScale;

            RenderUtils.AddParticle(level, new UltimateSlashTrail(level,
                    sx + x,
                    sy + y + 1.2,
                    sz + z,
                    (-ex - sx),
                    ey-sy,
                    (-ez-sz)));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new UltimateSlashStrikeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
