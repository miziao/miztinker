package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.modifiers.base.SmelteryBoostHelper;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.recipe.melting.OreMeltingRecipe; // 重新切回矿石专用子类
import slimeknights.tconstruct.common.config.Config;

@Mixin(value = OreMeltingRecipe.class, remap = false)
public class OreMeltingRecipeMixin {

    @Inject(method = "getOutput(Lslimeknights/tconstruct/library/recipe/melting/IMeltingContainer;)Lnet/minecraftforge/fluids/FluidStack;",
            at = @At("RETURN"),
            cancellable = true)
    private void boostYield(IMeltingContainer inv, CallbackInfoReturnable<FluidStack> cir) {
        // --- 核心调试日志 ---
        System.out.println(">>>> [MIZTINKER_DEBUG] 矿石熔炼 Mixin 触发成功！");

        FluidStack original = cir.getReturnValue();
        if (original != null && !original.isEmpty()) {
            // 这里我们暂时注释掉结构检测，只要是矿石就翻倍，用来测试 Mixin 是否生效
            if (SmelteryBoostHelper.hasBoostBlockInStructure(inv)) {

                float multiplier = Config.COMMON.repairKitAmount.get().floatValue();
                int newAmount = (int) (original.getAmount() * multiplier);

                System.out.println(">>>> [MIZTINKER_DEBUG] 矿石翻倍成功: 原始 " + original.getAmount() + " -> " + newAmount);

                FluidStack boosted = new FluidStack(original.getFluid(), newAmount);
                cir.setReturnValue(boosted);
                // }
            }
        }
    }
}