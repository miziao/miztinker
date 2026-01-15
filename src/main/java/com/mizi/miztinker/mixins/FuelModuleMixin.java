package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.modifiers.base.EternalFuelHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;

import java.lang.reflect.Field;

@Mixin(value = FuelModule.class, remap = false)
public abstract class FuelModuleMixin {
    @Shadow protected int fuel;
    @Shadow protected int temperature;
    @Shadow @Final protected LazyOptional<IFluidHandler> fluidHandler;
    @Shadow protected abstract MeltingFuel findRecipe(Fluid fluid);

    @Unique private Field miz$parentField = null;

    @Inject(method = "decreaseFuel(I)V", at = @At("HEAD"), cancellable = true)
    private void preventFuelConsumption(int amount, CallbackInfo ci) {
        BlockEntity parentEntity = miz$getParent();
        if (EternalFuelHelper.isEternalFuelActive(parentEntity)) {
            if (this.fuel < 20) {
                this.fuel = 100;
            }
            ci.cancel();
            parentEntity.setChanged();
        }
    }

    @Inject(method = "getTemperature()I", at = @At("RETURN"), cancellable = true)
    private void getTemperatureFromTank(CallbackInfoReturnable<Integer> cir) {
        BlockEntity parentEntity = miz$getParent();
        if (EternalFuelHelper.isEternalFuelActive(parentEntity)) {
            int tankTemp = miz$getTankTemperature();
            if (tankTemp > 0) {
                this.temperature = tankTemp;
                cir.setReturnValue(tankTemp);
            }
        }
    }

    @Unique
    protected int miz$getTankTemperature() {
        if (fluidHandler != null && fluidHandler.isPresent()) {
            IFluidHandler handler = fluidHandler.orElse(EmptyFluidHandler.INSTANCE);
            FluidStack fluid = handler.getFluidInTank(0);
            if (!fluid.isEmpty()) {
                MeltingFuel recipe = findRecipe(fluid.getFluid());
                if (recipe != null) return recipe.getTemperature();
            }
        }
        return 0;
    }

    @Unique
    protected BlockEntity miz$getParent() {
        try {
            if (miz$parentField == null) {
                for (Field f : FuelModule.class.getDeclaredFields()) {
                    if (BlockEntity.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        miz$parentField = f;
                        break;
                    }
                }
            }
            return (BlockEntity) miz$parentField.get(this);
        } catch (Exception ignored) {}
        return null;
    }
}