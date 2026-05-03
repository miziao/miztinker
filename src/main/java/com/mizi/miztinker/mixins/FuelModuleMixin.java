package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.IFuelModuleMiziHelper;
import com.mizi.miztinker.util.SmelteryComponentHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;

@Mixin(value = FuelModule.class, remap = false)
public abstract class FuelModuleMixin implements IFuelModuleMiziHelper {
    @Shadow protected int fuel;
    @Shadow protected int temperature;
    @Shadow protected LazyOptional<IFluidHandler> fluidHandler;
    @Shadow protected abstract MeltingFuel findRecipe(Fluid fluid);
    @Shadow @Final protected MantleBlockEntity parent;

    @Override
    public MeltingFuel mizi$getLiveRecipe() {
        if (fluidHandler != null && fluidHandler.isPresent()) {
            IFluidHandler handler = fluidHandler.orElse(EmptyFluidHandler.INSTANCE);
            FluidStack fluid = handler.getFluidInTank(0);
            if (!fluid.isEmpty()) {
                return findRecipe(fluid.getFluid());
            }
        }
        return null;
    }

    @Inject(method = "decreaseFuel(I)V", at = @At("HEAD"), cancellable = true)
    private void mizi$preventFuelConsumption(int amount, CallbackInfo ci) {
        if (SmelteryComponentHelper.isEternalFuelActive(this.parent)) {
            if (this.fuel < 20) this.fuel = 100;
            ci.cancel();
            this.parent.setChanged();
        }
    }

    @Inject(method = "getTemperature()I", at = @At("RETURN"), cancellable = true)
    private void mizi$getTemperatureFromTank(CallbackInfoReturnable<Integer> cir) {
        if (SmelteryComponentHelper.isEternalFuelActive(this.parent)) {
            MeltingFuel live = mizi$getLiveRecipe();
            if (live != null) {
                this.temperature = live.getTemperature();
                cir.setReturnValue(this.temperature);
            }
        }
    }
}