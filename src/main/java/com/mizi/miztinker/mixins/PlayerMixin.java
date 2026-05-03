package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.modifiers.EnchantedGold;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void miztinker$canEatAlways(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (!ignoreHunger && EnchantedGold.hasEnchantedGold(player)) {
            cir.setReturnValue(true);
        }
    }
}