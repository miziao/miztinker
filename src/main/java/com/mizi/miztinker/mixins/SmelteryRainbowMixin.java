package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.register.MiztinkerFluidRegister;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.tank.SmelteryTank;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = HeatingStructureBlockEntity.class, remap = false)
public abstract class SmelteryRainbowMixin {

    @Inject(method = "notifyFluidsChanged", at = @At("TAIL"))
    private void mizi$onFluidsUpdated(CallbackInfo ci) {
        HeatingStructureBlockEntity smeltery = (HeatingStructureBlockEntity) (Object) this;

        if (smeltery.getLevel() == null || smeltery.getLevel().isClientSide) {
            return;
        }

        SmelteryTank<?> tank = smeltery.getTank();
        int tankCount = tank.getTanks();

        if (tankCount < 20) {
            return;
        }

        Set<Fluid> uniqueFluids = new HashSet<>();
        for (int i = 0; i < tankCount; i++) {
            FluidStack stack = tank.getFluidInTank(i);
            if (!stack.isEmpty()) {
                uniqueFluids.add(stack.getFluid());
            }
        }

        if (uniqueFluids.size() >= 20) {
            mizi$executeConversion(tank);
        }
    }


    @Unique
    private void mizi$executeConversion(SmelteryTank<?> tank) {
        for (int i = tank.getTanks() - 1; i >= 0; i--) {
            FluidStack current = tank.getFluidInTank(i);
            if (!current.isEmpty()) {
                tank.drain(current, IFluidHandler.FluidAction.EXECUTE);
            }
        }

        FluidStack rainbow = new FluidStack(MiztinkerFluidRegister.RAINBOW_MATERIAL.get(), 900);
        tank.fill(rainbow, IFluidHandler.FluidAction.EXECUTE);

        ((HeatingStructureBlockEntity)(Object)this).setChanged();
    }
}