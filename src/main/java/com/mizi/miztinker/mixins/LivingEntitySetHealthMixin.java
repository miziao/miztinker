package com.mizi.miztinker.mixins;

import com.mizi.miztinker.entity.boss.entity.MiziAo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.mizi.miztinker.modifier.modifiers.base.MizUtil.hasAll_Perfect;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySetHealthMixin {

    @ModifyVariable(method = "setHealth", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyHealthValue(float health) {
        LivingEntity entity = (LivingEntity) (Object) this;
        float current = entity.getHealth();
        float damage = current - health;

        if (entity instanceof MiziAo) {
            if (damage > 101.0f) {
                return current - 101.0f;
            }
        }

        if (entity instanceof Player player && player.isAddedToWorld()) {
            if (hasAll_Perfect(player)) {
                if (damage > 101.0f) {
                    return current - 101.0f;
                }
            }
        }

        return health;
    }
}