package com.mizi.miztinker.mixins;

import com.mizi.miztinker.client.MizShaderClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void miztinker$freezeParticlesDuringTimeStop(CallbackInfo ci) {
        if (MizShaderClient.isTimeStopVisualActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void miztinker$blockNewParticlesDuringTimeStop(Particle particle, CallbackInfo ci) {
        if (MizShaderClient.isTimeStopVisualActive()) {
            ci.cancel();
        }
    }
}