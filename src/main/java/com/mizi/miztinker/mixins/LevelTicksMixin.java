package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.MizTimeStopHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(LevelTicks.class)
public abstract class LevelTicksMixin<T> {

    @Inject(
            method = "tick(JILjava/util/function/BiConsumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void miztinker$freezeScheduledTicksDuringTimeStop(
            long gameTime,
            int maxAllowedTicks,
            BiConsumer<BlockPos, T> ticker,
            CallbackInfo ci
    ) {
        if (MizTimeStopHandler.isTimeStopped()) {
            ci.cancel();
        }
    }
}