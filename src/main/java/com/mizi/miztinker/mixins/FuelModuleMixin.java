package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.IFuelModuleMiziHelper;
import com.mizi.miztinker.util.SmelteryComponentHelper;
import net.minecraft.world.level.Level;
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
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;

@Mixin(value = FuelModule.class, remap = false)
public abstract class FuelModuleMixin implements IFuelModuleMiziHelper {
    @Shadow protected int fuel;
    @Shadow protected int fuelQuality;
    @Shadow protected int temperature;
    @Shadow protected int rate;
    @Shadow protected LazyOptional<IFluidHandler> fluidHandler;
    @Shadow @Final protected MantleBlockEntity parent;

    @Shadow protected abstract MeltingFuel findRecipe(Fluid fluid);
    @Shadow public abstract int findFuel(boolean consume);

    @Unique private static final int MIZI_FUEL = 0;
    @Unique private static final int MIZI_FUEL_QUALITY = 1;
    @Unique private static final int MIZI_TEMPERATURE = 2;
    @Unique private static final int MIZI_RATE = 3;
    @Unique private static final int MIZI_SYNC_INTERVAL = 5;

    @Unique private long mizi$lastClientSync = Long.MIN_VALUE;

    @Override
    public MeltingFuel mizi$getLiveRecipe() {
        return mizi$getRecipeFromHandler(mizi$getCurrentHandler());
    }

    @Inject(
            method = "tryLiquidFuel(Lnet/minecraftforge/fluids/capability/IFluidHandler;Z)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mizi$tryEternalLiquidFuel(IFluidHandler handler, boolean consume, CallbackInfoReturnable<Integer> cir) {
        if (!mizi$isEternalActive()) {
            return;
        }

        MeltingFuel recipe = mizi$getRecipeFromHandler(handler);
        if (recipe == null) {
            return;
        }

        mizi$applyEternalFuel(recipe);
        mizi$markFuelChanged();
        cir.setReturnValue(this.temperature);
    }

    @Inject(method = "hasFuel()Z", at = @At("HEAD"), cancellable = true)
    private void mizi$hasEternalFuel(CallbackInfoReturnable<Boolean> cir) {
        if (mizi$refreshEternalFuel()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "decreaseFuel(I)V", at = @At("RETURN"))
    private void mizi$keepEternalFuelFull(int amount, CallbackInfo ci) {
        if (mizi$refreshEternalFuel()) {
            mizi$markFuelChanged();
        }
    }

    @Inject(method = "getFuel()I", at = @At("HEAD"), cancellable = true)
    private void mizi$getVisibleFuel(CallbackInfoReturnable<Integer> cir) {
        if (mizi$refreshEternalFuel()) {
            cir.setReturnValue(mizi$getFullFuelValue());
        }
    }

    @Inject(method = "getFuelQuality()I", at = @At("HEAD"), cancellable = true)
    private void mizi$getVisibleFuelQuality(CallbackInfoReturnable<Integer> cir) {
        if (mizi$refreshEternalFuel()) {
            cir.setReturnValue(mizi$getFullFuelValue());
        }
    }

    @Inject(method = "getTemperature()I", at = @At("HEAD"), cancellable = true)
    private void mizi$getEternalTemperature(CallbackInfoReturnable<Integer> cir) {
        if (!mizi$isEternalActive()) {
            return;
        }

        MeltingFuel recipe = mizi$getLiveRecipe();
        if (recipe != null) {
            mizi$applyEternalFuel(recipe);
            cir.setReturnValue(this.temperature);
            return;
        }

        if (this.temperature > 0) {
            cir.setReturnValue(this.temperature);
        }
    }

    @Inject(method = "getRate()I", at = @At("HEAD"), cancellable = true)
    private void mizi$getEternalRate(CallbackInfoReturnable<Integer> cir) {
        if (!mizi$isEternalActive()) {
            return;
        }

        MeltingFuel recipe = mizi$getLiveRecipe();
        if (recipe != null) {
            mizi$applyEternalFuel(recipe);
            cir.setReturnValue(this.rate);
            return;
        }

        if (this.rate > 0) {
            cir.setReturnValue(this.rate);
        }
    }

    @Inject(
            method = "getFuelInfo()Lslimeknights/tconstruct/smeltery/block/entity/module/FuelModule$FuelInfo;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mizi$getEternalFuelInfo(CallbackInfoReturnable<FuelModule.FuelInfo> cir) {
        if (!mizi$refreshEternalFuel()) {
            return;
        }

        IFluidHandler handler = mizi$getCurrentHandler();
        if (handler == null) {
            return;
        }

        FluidStack fluid = handler.getFluidInTank(0);
        if (fluid.isEmpty()) {
            return;
        }

        int capacity = Math.max(1, handler.getTankCapacity(0));
        FluidStack displayFluid = fluid.copy();
        displayFluid.setAmount(Math.max(displayFluid.getAmount(), capacity));
        cir.setReturnValue(FuelModule.FuelInfo.of(displayFluid, capacity, this.temperature));
    }

    @Inject(
            method = "get(I)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void mizi$getSyncedEternalFuelData(int index, CallbackInfoReturnable<Integer> cir) {
        if (!mizi$refreshEternalFuel()) {
            return;
        }

        if (index == MIZI_FUEL || index == MIZI_FUEL_QUALITY) {
            cir.setReturnValue(mizi$getFullFuelValue());
        } else if (index == MIZI_TEMPERATURE) {
            cir.setReturnValue(this.temperature);
        } else if (index == MIZI_RATE) {
            cir.setReturnValue(this.rate);
        }
    }

    @Unique
    private boolean mizi$refreshEternalFuel() {
        if (!mizi$isEternalActive()) {
            return false;
        }

        MeltingFuel recipe = mizi$getLiveRecipe();
        if (recipe != null) {
            mizi$applyEternalFuel(recipe);
            return true;
        }

        return this.findFuel(false) > 0 && this.temperature > 0 && this.rate > 0;
    }

    @Unique
    private boolean mizi$isEternalActive() {
        return SmelteryComponentHelper.isEternalFuelActive(this.parent);
    }

    @Unique
    private IFluidHandler mizi$getCurrentHandler() {
        if (this.fluidHandler == null || !this.fluidHandler.isPresent()) {
            return null;
        }
        return this.fluidHandler.orElse(EmptyFluidHandler.INSTANCE);
    }

    @Unique
    private MeltingFuel mizi$getRecipeFromHandler(IFluidHandler handler) {
        if (handler == null) {
            return null;
        }

        FluidStack fluid = handler.getFluidInTank(0);
        if (fluid.isEmpty()) {
            return null;
        }

        return findRecipe(fluid.getFluid());
    }

    @Unique
    private void mizi$applyEternalFuel(MeltingFuel recipe) {
        int duration = Math.max(1, recipe.getDuration());
        this.fuel = duration;
        this.fuelQuality = duration;
        this.temperature = recipe.getTemperature();
        this.rate = recipe.getRate();
    }

    @Unique
    private int mizi$getFullFuelValue() {
        return Math.max(1, Math.max(this.fuel, this.fuelQuality));
    }

    @Unique
    private void mizi$markFuelChanged() {
        Level level = this.parent.getLevel();
        if (level == null || !level.isClientSide) {
            this.parent.setChangedFast();
            mizi$syncFuelToClient(level);
        }
    }

    @Unique
    private void mizi$syncFuelToClient(Level level) {
        if (level == null || level.isClientSide) {
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime - this.mizi$lastClientSync < MIZI_SYNC_INTERVAL) {
            return;
        }

        this.mizi$lastClientSync = gameTime;
        level.sendBlockUpdated(this.parent.getBlockPos(), this.parent.getBlockState(), this.parent.getBlockState(), 3);
    }

    @Inject(method = "set(II)V", at = @At("RETURN"), remap = true)
    private void mizi$acceptSyncedFuelData(int index, int value, CallbackInfo ci) {
        if (index == MIZI_FUEL && value > 0 && this.fuelQuality <= 0) {
            this.fuelQuality = value;
        } else if (index == MIZI_FUEL_QUALITY && value > 0 && this.fuel <= 0) {
            this.fuel = value;
        }
    }
}
