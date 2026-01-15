package com.mizi.miztinker.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

@Mixin(Player.class)
public class MixinPlayerPermission {

    @Inject(method = "canUseGameMasterBlocks", at = @At("HEAD"), cancellable = true)
    private void allowCommandBlockUsage(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        ModifierId cmdId = new ModifierId("miztinker", "command");

        boolean hasTrait = false;
        for (int i = 0; i < 9; i++) {
            if (ModifierUtil.getModifierLevel(player.getInventory().getItem(i), cmdId) > 0) {
                hasTrait = true; break;
            }
        }
        if (!hasTrait) {
            for (ItemStack armor : player.getArmorSlots()) {
                if (ModifierUtil.getModifierLevel(armor, cmdId) > 0) {
                    hasTrait = true; break;
                }
            }
        }

        if (hasTrait) {
            cir.setReturnValue(true);
        }
    }
}