package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.SmelteryComponentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.alloying.IAlloyTank;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.alloying.MultiAlloyingModule;
import slimeknights.tconstruct.smeltery.block.entity.tank.SmelteryTank;

import java.util.List;

@Mixin(value = MultiAlloyingModule.class, remap = false)
public abstract class SmelteryAlloyRetarderMixin {

    @Shadow @Final private MantleBlockEntity parent;
    @Shadow @Final private IAlloyTank alloyTank;
    @Shadow protected abstract List<AlloyRecipe> getRecipes();
    @Shadow protected abstract Level getLevel();

    @Inject(method = "canAlloy", at = @At("HEAD"), cancellable = true)
    private void mizi$optimizedCanAlloy(CallbackInfoReturnable<Boolean> cir) {
        if (!(this.parent instanceof HeatingStructureBlockEntity smeltery)) return;

        if (SmelteryComponentHelper.isRetarderActive(smeltery)) {
            List<AlloyRecipe> recipes = this.getRecipes();
            if (recipes.isEmpty()) return;

            Level world = this.getLevel();
            for (AlloyRecipe recipe : recipes) {
                if (recipe.matches(this.alloyTank, world)) {
                    Fluid resultFluid = recipe.getOutput().getFluid();
                    if (mizi$hasFluidInTank(smeltery, resultFluid)) {
                        return;
                    }
                }
            }
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean mizi$hasFluidInTank(HeatingStructureBlockEntity smeltery, Fluid target) {
        SmelteryTank<?> tank = smeltery.getTank();
        for (int i = 0; i < tank.getTanks(); i++) {
            if (tank.getFluidInTank(i).getFluid() == target) {
                return true;
            }
        }
        return false;
    }
}