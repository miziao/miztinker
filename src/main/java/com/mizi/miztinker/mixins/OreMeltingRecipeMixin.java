package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.SmelteryComponentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.recipe.melting.OreMeltingRecipe;
import slimeknights.tconstruct.common.config.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(value = OreMeltingRecipe.class, remap = false)
public class OreMeltingRecipeMixin {

    @Inject(method = "getOutput(Lslimeknights/tconstruct/library/recipe/melting/IMeltingContainer;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("RETURN"),
            cancellable = true)
    private void mizi$boostOreYield(IMeltingContainer inv, CallbackInfoReturnable<FluidStack> cir) {
        FluidStack original = cir.getReturnValue();

        if (original != null && !original.isEmpty()) {
            BlockEntity controller = mizi$getController(inv);
            if (controller == null) return;

            float totalMultiplier = SmelteryComponentHelper.getProductionMultiplier(controller);
            float configBase = Config.COMMON.repairKitAmount.get().floatValue();

            if (totalMultiplier != configBase) {
                float safeConfigBase = Math.max(configBase, 0.001f);
                int baseAmount = (int) (original.getAmount() / safeConfigBase);

                int newAmount = (int) (baseAmount * totalMultiplier);

                cir.setReturnValue(new FluidStack(original.getFluid(), newAmount));
            }
        }
    }


    @Unique
    private BlockEntity mizi$getController(IMeltingContainer inv) {
        try {
            try {
                Method getParentMethod = inv.getClass().getMethod("getParent");
                Object obj = getParentMethod.invoke(inv);
                if (obj instanceof BlockEntity be) return be;
            } catch (NoSuchMethodException e) {
                Field parentField = inv.getClass().getDeclaredField("parent");
                parentField.setAccessible(true);
                Object obj = parentField.get(inv);
                if (obj instanceof BlockEntity be) return be;
            }
        } catch (Exception ignored) {}
        return null;
    }
}