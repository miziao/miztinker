package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.modifiers.base.SmelteryBoostHelper;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.recipe.melting.OreMeltingRecipe;
import slimeknights.tconstruct.common.config.Config;

@Mixin(value = OreMeltingRecipe.class, remap = false)
public class OreMeltingRecipeMixin {

    @Inject(method = "getOutput(Lslimeknights/tconstruct/library/recipe/melting/IMeltingContainer;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("RETURN"),
            cancellable = true)
    private void boostYield(IMeltingContainer inv, CallbackInfoReturnable<FluidStack> cir) {
        FluidStack original = cir.getReturnValue();

        if (original != null && !original.isEmpty()) {
            float totalMultiplier = SmelteryBoostHelper.getSmelteryMultiplier(inv);

            float configBase = Config.COMMON.repairKitAmount.get().floatValue();

            if (totalMultiplier != configBase) {
                int baseAmount = (int) (original.getAmount() / configBase);
                int newAmount = (int) (baseAmount * totalMultiplier);

                FluidStack boosted = new FluidStack(original.getFluid(), newAmount);
                cir.setReturnValue(boosted);
            }
        }
    }
}