package com.mizi.miztinker.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class AscendingCutParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected AscendingCutParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pQuadSizeMultiplier, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, 0F, 0F, 0F);
        this.quadSize = (1F - (float)pQuadSizeMultiplier * 0.25F)*2.5F;
        this.lifetime = 4;
        this.alpha =0.85F;
        this.sprites = pSprites;

        this.setSize(1F, 1F);
        this.setColor(0.95F, 0.0F, 0.02F);
        this.setSpriteFromAge(pSprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }

    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
            AscendingCutParticle particle = new AscendingCutParticle(level, x, y, z, xSpeed, this.spriteSet);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}
