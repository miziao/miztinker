package com.mizi.miztinker.mixins;

import com.mizi.miztinker.renderer.murasama.PostEffectPipelines;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V",
                    ordinal = 0
            ))
    private void PostRender(float pt, long startTime, boolean tick, CallbackInfo cbi){
        PostEffectPipelines.RenderPost();
    }

}