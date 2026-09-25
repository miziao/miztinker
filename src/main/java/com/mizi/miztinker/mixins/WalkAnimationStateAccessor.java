package com.mizi.miztinker.mixins;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
    @Accessor("speedOld")
    float miztinker$getSpeedOld();

    @Accessor("speedOld")
    void miztinker$setSpeedOld(float value);

    @Accessor("speed")
    float miztinker$getSpeed();

    @Accessor("speed")
    void miztinker$setSpeed(float value);

    @Accessor("position")
    float miztinker$getPosition();

    @Accessor("position")
    void miztinker$setPosition(float value);
}