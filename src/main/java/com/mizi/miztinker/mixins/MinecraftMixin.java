package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.Time;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Nullable public ClientLevel level;
    @Shadow @Nullable public LocalPlayer player;
    @Shadow
    public ProfilerFiller profiler;
    @Shadow @Nullable public Screen screen;
    @Shadow public volatile boolean pause;
    @Shadow
    public int rightClickDelay;

    @Shadow public abstract void setScreen(@Nullable Screen p_91153_);
    @Shadow protected abstract void handleKeybinds();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        if (Time.get()) {
            this.pause = true;

            if (this.rightClickDelay > 0) this.rightClickDelay--;
            this.profiler.push("gui");
            if (this.screen != null) {
                Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
            }
            this.profiler.pop();

            if (this.screen == null && this.player != null) {
                this.handleKeybinds();
            }

            if (this.level != null) {
                this.level.entitiesForRendering().forEach((entity) -> {
                    if (!entity.isRemoved() && !entity.isPassenger()) {
                        if (entity instanceof net.minecraft.world.entity.player.Player && entity != this.player) {
                            this.level.guardEntityTick(this.level::tickNonPassenger, entity);
                        }
                        if (entity.tickCount < 1) {
                            this.level.guardEntityTick(this.level::tickNonPassenger, entity);
                        }
                    }
                });
            }

            ci.cancel();
        }
    }
}