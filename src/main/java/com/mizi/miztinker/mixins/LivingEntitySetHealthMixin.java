package com.mizi.miztinker.mixins;

import com.mizi.miztinker.entity.boss.entity.MiziAo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mizi.miztinker.modifier.modifiers.base.MizUtil.hasAll_Perfect;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySetHealthMixin {

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void onSetHealth(float health, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;

        float current = entity.getHealth();
        float damage = current - health;

        // --- MiziAo Boss 免疫大于1010伤害 ---
        if (entity instanceof MiziAo) {

            // 如果伤害 <= 1010，正常执行
            if (damage <= 101f) {
                return;
            }

            // 否则强制把伤害改为 1010（避免一击秒杀）
            float limitedHealth = current - 101f;

            // 不使用 setHealth，直接修改实体 Data
            entity.setHealth(limitedHealth);

            // 取消原本的 setHealth 调用，避免覆盖
            ci.cancel();
        }

        if (!(entity instanceof Player player) || !player.isAddedToWorld()) {
            return;
        }

        if (hasAll_Perfect(player)) {
            float damageAmount = player.getHealth() - health;
            if (damageAmount > 101.0f) {
                ci.cancel();
            }
        }
    }
}