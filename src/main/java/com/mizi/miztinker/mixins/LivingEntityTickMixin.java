package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void stopTickIfHorologium(CallbackInfo ci) {
        if (!((Object)this instanceof LivingEntity self)) return;

        if (self.hasEffect(MiztinkerEffect.HorologiumNoAI.get())) {
            if (self instanceof Mob mob) {
                mob.setNoAi(true);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void resetNoAiFlag(CallbackInfo ci) {
        if (!((Object)this instanceof LivingEntity self)) return;

        if (self instanceof Mob mob) {
            mob.setNoAi(false);
        }
    }
}