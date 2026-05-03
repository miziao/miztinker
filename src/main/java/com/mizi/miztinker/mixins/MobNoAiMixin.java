package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import com.mizi.miztinker.util.MizTimeStopHandler;
import com.mizi.miztinker.util.Time;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobNoAiMixin {

    @Inject(method = "isNoAi", at = @At("HEAD"), cancellable = true)
    private void forceNoAiWhenTimeStopped(CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob) (Object) this;

        if (self.hasEffect(MiztinkerEffect.HorologiumNoAI.get())) {
            cir.setReturnValue(true);
            return;
        }

        if (MizTimeStopHandler.isEntityStopped(self)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isNoAi", at = @At("HEAD"), cancellable = true)
    private void forceNoAi(CallbackInfoReturnable<Boolean> cir) {
        if (Time.get()) {
            cir.setReturnValue(true);
        }
    }
}