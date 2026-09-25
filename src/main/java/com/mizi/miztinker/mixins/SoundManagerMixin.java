package com.mizi.miztinker.mixins;

import com.mizi.miztinker.client.MizShaderClient;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {

    @Inject(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void miztinker$blockSoundsDuringTimeStop(SoundInstance sound, CallbackInfo ci) {
        if (MizShaderClient.shouldBlockNewTimeStopSounds(sound)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void miztinker$blockDelayedSoundsDuringTimeStop(SoundInstance sound, int delay, CallbackInfo ci) {
        if (MizShaderClient.shouldBlockNewTimeStopSounds(sound)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "queueTickingSound(Lnet/minecraft/client/resources/sounds/TickableSoundInstance;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void miztinker$blockTickingSoundsDuringTimeStop(TickableSoundInstance sound, CallbackInfo ci) {
        if (MizShaderClient.shouldBlockNewTimeStopSounds(sound)) {
            ci.cancel();
        }
    }
}