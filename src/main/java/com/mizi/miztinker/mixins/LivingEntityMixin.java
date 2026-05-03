package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 999)
public abstract class LivingEntityMixin {

    @Shadow public float lastHurt;
    @Shadow public abstract MobEffectInstance getEffect(MobEffect effect);


    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private float miztinker$ensureMinDamage(float amount, DamageSource source) {
        if (source.getEntity() instanceof Player && amount <= 0.0F) {
            return 0.1F;
        }
        return amount;
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void miztinker$resetLastHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof Player) {
            LivingEntity self = (LivingEntity)(Object)this;
            if (self.lastHurt > 0) {
                self.lastHurt = 0.0F;
            }
        }
    }

    @Inject(method = "removeEffect", at = @At("HEAD"), cancellable = true)
    private void miztinker$preventKamuiRemoval(MobEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect == MiztinkerEffect.KAMUI_PLUS.get()) {
            LivingEntity self = (LivingEntity) (Object) this;
            MobEffectInstance instance = self.getEffect(MiztinkerEffect.KAMUI_PLUS.get());

            if (instance != null && instance.getDuration() > 0) {
                if (!com.mizi.miztinker.effect.Pair_Kamui_effect.BYPASS_THREAD_LOCAL.get()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}