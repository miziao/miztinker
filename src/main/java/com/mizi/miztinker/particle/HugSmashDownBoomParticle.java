package com.mizi.miztinker.particle;

import com.mizi.miztinker.particle.register.MiztinkerParticlesRegister;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class HugSmashDownBoomParticle extends HugeExplosionParticle {

    private int life;

    protected HugSmashDownBoomParticle(ClientLevel level, double x, double y, double z, double pQuadSizeMultiplier, SpriteSet pSprites) {
        super(level, x, y, z, pQuadSizeMultiplier,pSprites);
        this.quadSize = 2.0F * (1.0F - (float)pQuadSizeMultiplier * 0.5F);
        this.lifetime =2;
//        this.alpha =0;

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
        for(int i = 0; i < 24; ++i) {
            double d0 = this.x + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
            double d1 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
            double d2 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * (double)4.0F;
            SimpleParticleType var10001 = MiztinkerParticlesRegister.smash_down_boom.get();
            float var10005 = (float)this.life;
            this.level.addParticle(var10001, d0, d1, d2, var10005 / 2, 0F, 0F);
        }

        ++this.life;
        int var8 = this.life;
        if (var8 == 2) {
            this.remove();
        }

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
            HugSmashDownBoomParticle particle = new HugSmashDownBoomParticle(level, x, y, z, xSpeed, this.spriteSet);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}
