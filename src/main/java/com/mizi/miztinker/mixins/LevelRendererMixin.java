package com.mizi.miztinker.mixins;

import com.mizi.miztinker.client.MizShaderClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void miztinker$freezeLevelRendererTickDuringTimeStop(CallbackInfo ci) {
        if (MizShaderClient.isTimeStopVisualActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
    private void miztinker$freezeRainTickDuringTimeStop(Camera camera, CallbackInfo ci) {
        if (MizShaderClient.isTimeStopVisualActive()) {
            ci.cancel();
        }
    }

    @ModifyVariable(
            method = "renderSnowAndRain",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float miztinker$freezeWeatherPartialTick(float partialTick) {
        if (MizShaderClient.isTimeStopVisualActive()) {
            return 0.0F;
        }

        return partialTick;
    }
}