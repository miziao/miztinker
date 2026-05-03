package com.mizi.miztinker.mixins;

import com.mizi.miztinker.util.SmelteryComponentHelper;
import com.mizi.miztinker.util.SmelteryUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.smeltery.block.entity.controller.FoundryBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.SmelteryBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.MultiblockStructureData;

@Mixin(value = HeatingStructureMultiblock.class, remap = false)
public class SmelteryValidatorMixin {
    @Inject(method = "shouldUpdate", at = @At("HEAD"), cancellable = true)
    private void mizi$forceUpdate(Level world, MultiblockStructureData structure, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (structure.getBounds().inflate(1).contains(pos.getX(), pos.getY(), pos.getZ())) {
            cir.setReturnValue(true);
        }
    }
}

@Mixin(value = SmelteryBlockEntity.class, remap = false)
abstract class SmelteryCapacityMixin {
    @Inject(method = "setStructure", at = @At("TAIL"))
    private void mizi$onSmelterySetStructure(StructureData structure, CallbackInfo ci) {
        SmelteryBlockEntity self = (SmelteryBlockEntity)(Object)this;
        if (structure != null && structure.getInnerX() > 0) {
            int reinforcedCount = SmelteryComponentHelper.getReinforcedBrickCount(self);
            int newCap = SmelteryUtility.calculateSmelteryCapacity(structure.getInnerX(), structure.getInnerY(), structure.getInnerZ(), reinforcedCount);

            if (self.getTank().getCapacity() != newCap) {
                SmelteryUtility.syncCapacity(self, newCap);
            }
        } else if (structure == null) {
            SmelteryUtility.handleBreak(self);
        }
    }
}

@Mixin(value = FoundryBlockEntity.class, remap = false)
abstract class FoundryCapacityMixin {
    @Inject(method = "setStructure", at = @At("TAIL"))
    private void mizi$onFoundrySetStructure(StructureData structure, CallbackInfo ci) {
        FoundryBlockEntity self = (FoundryBlockEntity)(Object)this;
        if (structure != null && structure.getInnerX() > 0) {
            int reinforcedCount = SmelteryComponentHelper.getReinforcedBrickCount(self);
            int newCap = SmelteryUtility.calculateFoundryCapacity(structure.getInnerX(), structure.getInnerY(), structure.getInnerZ(), reinforcedCount);

            if (self.getTank().getCapacity() != newCap) {
                SmelteryUtility.syncCapacity(self, newCap);
            }
        } else if (structure == null) {
            SmelteryUtility.handleBreak(self);
        }
    }
}