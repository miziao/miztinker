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
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.hasEffect(MiztinkerEffect.HorologiumNoAI.get())) {
            if (self instanceof Mob mob) {
                mob.setNoAi(true); // 暂停AI
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void resetNoAiFlag(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self instanceof Mob mob) {
            mob.setNoAi(false); // 无论效果是否存在，都恢复AI
        }
    }
}