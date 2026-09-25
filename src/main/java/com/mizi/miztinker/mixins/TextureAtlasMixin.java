package com.mizi.miztinker.mixins;

import com.mizi.miztinker.client.MizShaderClient;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void miztinker$freezeTextureAnimationsDuringTimeStop(CallbackInfo ci) {
        if (MizShaderClient.isTimeStopVisualActive()) {
            ci.cancel();
        }
    }
}