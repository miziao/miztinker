package com.mizi.miztinker.mixins;

import com.mizi.miztinker.effect.WoundEffect;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mizi.miztinker.effect.WoundEffectDataHandler.*;

/**
 * 用于实现“禁疗”效果的血量同步限制逻辑。
 * 当实体带有 {@link WoundEffect} 时：
 *   - 禁止血量上升（在 set 阶段处理）
 *   - 读取血量时返回 min(当前血量, 禁疗记录)（在 get 阶段处理）
 *
 * ✅ 已避免与其它 Mixin（例如 SynchedEntityDataMixin）冲突，
 *    因为这里使用的是 @Inject 而非 @Redirect。
 */
@Mixin(value = SynchedEntityData.class, priority = 2000)
public abstract class WoundEffectHealthMixin {

    @Final
    @Shadow
    private net.minecraft.world.entity.Entity entity;

    @Unique
    private boolean inSet = false;

    private <T> void handleSet(EntityDataAccessor<T> key, T value, CallbackInfo ci) {
        // 安全保护：实体或 SynchedEntityData 未初始化时直接返回
        if (inSet || entity == null || !(entity instanceof LivingEntity living)) return;
        if (living.getEntityData() == null) return;

        if (key.getSerializer() == EntityDataSerializers.FLOAT && value instanceof Float newHealth) {
            if (living.hasEffect(new WoundEffect())) {
                float lastHealth = getLastHealthSafe(living);
                float forced = Math.min(newHealth, lastHealth);

                inSet = true;
                try {
                    living.getEntityData().set((EntityDataAccessor<Float>) key, forced);
                } finally {
                    inSet = false;
                }
                ci.cancel();
            }
        }
    }

    @Inject(method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V",
            at = @At("HEAD"), cancellable = true)
    private <T> void onSet(EntityDataAccessor<T> key, T value, CallbackInfo ci) {
        handleSet(key, value, ci);
    }

    @Inject(method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;Z)V",
            at = @At("HEAD"), cancellable = true)
    private <T> void onSetWithForce(EntityDataAccessor<T> key, T value, boolean force, CallbackInfo ci) {
        handleSet(key, value, ci);
    }

    @Inject(method = "get(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;",
            at = @At("RETURN"), cancellable = true)
    private <T> void onGet(EntityDataAccessor<T> key, CallbackInfoReturnable<Object> cir) {
        if (entity == null || !(entity instanceof LivingEntity living) || living.getEntityData() == null) return;

        Object original = cir.getReturnValue();
        if (key.getSerializer() == EntityDataSerializers.FLOAT && living.hasEffect(new WoundEffect())) {
            float originalHealth = (float) original;
            float lastHealth = getLastHealthSafe(living);
            cir.setReturnValue(Math.min(originalHealth, lastHealth));
        }
    }

    @Unique
    private float getLastHealthSafe(LivingEntity entity) {
        try {
            return entity.getEntityData().get(WOUND_EFFECT_LAST_HEALTH);
        } catch (Exception e) {
            return entity.getHealth(); // fallback，防止 null
        }
    }
}