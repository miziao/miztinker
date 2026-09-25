package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.MizTimeStopHandler;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelBlockEntityTickMixin {
    @Shadow
    @Final
    public boolean isClientSide;

    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void miztinker$freezeBlockEntitiesDuringTimeStop(CallbackInfo ci) {
        if (!this.isClientSide && MizTimeStopHandler.isTimeStopped()) {
            ci.cancel();
        }
    }
}