package com.mizi.miztinker.mixins;

import com.refinedmods.refinedstorage.apiimpl.network.item.NetworkItemManager;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkItemManager.class, remap = false)
public abstract class RSNetworkItemManagerMixin {

    @Inject(method = "open", at = @At("HEAD"), cancellable = true, remap = false)
    private void mizi$forceOpen(Player player, ItemStack stack, PlayerSlot slot, CallbackInfo ci) {
        if (stack.hasTag() && stack.getOrCreateTag().getBoolean("is_tinker_proxy")) {
            ci.cancel();
        }
    }
}