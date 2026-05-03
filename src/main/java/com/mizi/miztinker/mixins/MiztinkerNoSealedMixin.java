package com.mizi.miztinker.mixins;

import dev.xkmc.l2hostility.compat.curios.EntitySlotAccess;
import dev.xkmc.l2hostility.content.traits.legendary.RagnarokTrait;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

@Mixin(value = RagnarokTrait.class, remap = false)
public abstract class MiztinkerNoSealedMixin {

    @Inject(at = @At("HEAD"), method = "allowSeal", cancellable = true)
    private static void mizi$allowSeal(EntitySlotAccess access, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = access.get();

        if (stack.isEmpty()) return;

        try {
            IToolStackView tool = ToolStack.from(stack);

            for (ModifierEntry entry : tool.getModifierList()) {
                if (entry.getModifier().getId().getNamespace().equals("miztinker")) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }
}