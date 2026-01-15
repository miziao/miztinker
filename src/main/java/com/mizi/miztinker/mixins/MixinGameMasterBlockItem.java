package com.mizi.miztinker.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.GameMasterBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

@Mixin(GameMasterBlockItem.class)
public class MixinGameMasterBlockItem {
    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void allowCommandBlockPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        Player player = context.getPlayer();
        if (player != null) {
            ModifierId cmdId = new ModifierId("miztinker", "command");
            if (ModifierUtil.getModifierLevel(player.getMainHandItem(), cmdId) > 0 ||
                    ModifierUtil.getModifierLevel(player.getOffhandItem(), cmdId) > 0) {

                BlockState state = ((GameMasterBlockItem)(Object)this).getBlock().getStateForPlacement(context);
                cir.setReturnValue(state);
            }
        }
    }
}