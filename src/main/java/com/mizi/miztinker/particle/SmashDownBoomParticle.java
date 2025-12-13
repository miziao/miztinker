package com.mizi.miztinker.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class SmashDownBoomParticle extends HugeExplosionParticle {
    protected SmashDownBoomParticle(ClientLevel level, double x, double y, double z, double pQuadSizeMultiplier, SpriteSet pSprites) {
        super(level, x, y, z, pQuadSizeMultiplier,pSprites);
        this.quadSize = 2.0F * (1.0F - (float)pQuadSizeMultiplier * 0.5F);
        this.lifetime = 6 + this.random.nextInt(4);
        this.alpha =0.75F;

        this.setSize(1F, 1F);
        this.setColor(0.95F, 0.0F, 0.02F);
        this.setSpriteFromAge(pSprites);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15 << 20 | 15 << 4;
    }


    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SmashDownBoomParticle particle = new SmashDownBoomParticle(level, x, y, z, xSpeed, this.spriteSet);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}
