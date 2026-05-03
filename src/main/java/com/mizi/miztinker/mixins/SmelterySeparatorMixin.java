package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.SmelteryComponentHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.tank.SmelteryTank;

import java.util.List;

@Mixin(value = HeatingStructureBlockEntity.class, remap = false)
public abstract class SmelterySeparatorMixin {

    @Inject(method = "notifyFluidsChanged", at = @At("TAIL"))
    private void mizi$onFluidsChangedForSeparation(CallbackInfo ci) {
        HeatingStructureBlockEntity smeltery = (HeatingStructureBlockEntity) (Object) this;

        if (smeltery.getLevel() == null || smeltery.getLevel().isClientSide) return;

        if (!SmelteryComponentHelper.isSeparatorActive(smeltery)) return;

        SmelteryTank<?> tank = smeltery.getTank();
        for (int i = 0; i < tank.getTanks(); i++) {
            FluidStack currentStack = tank.getFluidInTank(i);
            if (currentStack.isEmpty()) continue;

            AlloyRecipe recipe = mizi$findReverseAlloyRecipe(smeltery, currentStack);
            if (recipe != null) {
                int outputAmount = recipe.getOutput().getAmount();
                if (currentStack.getAmount() >= outputAmount) {
                    mizi$executeSeparation(smeltery, tank, currentStack, recipe);
                    break;
                }
            }
        }
    }

    @Unique
    private AlloyRecipe mizi$findReverseAlloyRecipe(HeatingStructureBlockEntity smeltery, FluidStack stack) {
        if (smeltery.getLevel() != null) {
            return smeltery.getLevel().getRecipeManager()
                    .getAllRecipesFor(TinkerRecipeTypes.ALLOYING.get())
                    .stream()
                    .filter(recipe -> recipe.getOutput().getFluid() == stack.getFluid())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Unique
    private void mizi$executeSeparation(HeatingStructureBlockEntity smeltery, SmelteryTank<?> tank, FluidStack resultStack, AlloyRecipe recipe) {
        int recipeOutputAmount = recipe.getOutput().getAmount();
        FluidStack toDrain = new FluidStack(resultStack.getFluid(), recipeOutputAmount);
        tank.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);

        recipe.getInputs().forEach(alloyIngredient -> {
            if (!alloyIngredient.catalyst()) {
                List<FluidStack> fluidList = alloyIngredient.fluid().getFluids();
                if (!fluidList.isEmpty()) {
                    FluidStack firstMatch = fluidList.get(0);
                    int ingredientAmount = alloyIngredient.fluid().getAmount(firstMatch.getFluid());
                    FluidStack ingredientStack = new FluidStack(firstMatch.getFluid(), ingredientAmount);
                    tank.fill(ingredientStack, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        });

        smeltery.setChanged();
        tank.syncFluids();
    }
}