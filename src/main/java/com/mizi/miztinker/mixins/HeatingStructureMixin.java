package com.mizi.miztinker.mixins;

import com.mizi.miztinker.block.TinkerElectricityModuleBlock.TinkerElectricityModuleBlockEntity;
import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import com.mizi.miztinker.util.IFuelModuleMiziHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = HeatingStructureBlockEntity.class, remap = false)
public abstract class HeatingStructureMixin {

    @Unique
    private final List<BlockPos> mizi$electricityModuleCache = new ArrayList<>();


    @Inject(method = "setStructure", at = @At("RETURN"))
    private void mizi$updateModuleCache(StructureData structure, CallbackInfo ci) {
        mizi$electricityModuleCache.clear();
        if (structure != null) {
            HeatingStructureBlockEntity self = (HeatingStructureBlockEntity)(Object)this;
            Level level = self.getLevel();
            if (level != null) {
                structure.forEachContained(pos -> {
                    if (level.getBlockState(pos).is(MiztinkerBlocks.TINKER_ELECTRICITY_MODULE.get())) {
                        mizi$electricityModuleCache.add(pos.immutable());
                    }
                });
            }
        }
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private void mizi$generateElectricity(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (mizi$electricityModuleCache.isEmpty()) return;

        HeatingStructureBlockEntity self = (HeatingStructureBlockEntity)(Object)this;
        FuelModule fuelModule = self.getFuelModule();

        if (fuelModule.getFuel() <= 0) return;

        MeltingFuel recipe = ((IFuelModuleMiziHelper) fuelModule).mizi$getLiveRecipe();

        if (recipe == null) return;
        long energyToGen = (long) recipe.getDuration() * recipe.getRate() * recipe.getTemperature();

        int finalEnergy = (energyToGen > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) energyToGen;

        for (BlockPos modulePos : mizi$electricityModuleCache) {
            BlockEntity be = level.getBlockEntity(modulePos);
            if (be instanceof TinkerElectricityModuleBlockEntity moduleBE) {
                moduleBE.receiveEnergyFromSmeltery(finalEnergy);
            }
        }
    }
}