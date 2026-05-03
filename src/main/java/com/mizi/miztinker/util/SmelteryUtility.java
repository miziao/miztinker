package com.mizi.miztinker.util;


import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

public class SmelteryUtility {

    public static int calculateSmelteryCapacity(int dx, int dy, int dz, int reinforcedCount) {
        long base = (long)dx * dy * dz * 1080L;
        long bonus = (long)reinforcedCount * 360L;
        return (int)Math.min(base + bonus, 2000000000L);
    }

    public static int calculateFoundryCapacity(int dx, int dy, int dz, int reinforcedCount) {
        long baseVol = (long)(dx + 2) * (dy + 1) * (dz + 2);
        long baseCap = 720L * baseVol;
        long bonus = (long)reinforcedCount * 720L;
        return (int)Math.min(baseCap + bonus, 2000000000L);
    }

    public static void syncCapacity(HeatingStructureBlockEntity self, int newCapacity) {
        self.getTank().setCapacity(newCapacity);
        self.setChangedFast();
        self.notifyFluidsChanged(ISmelteryTankHandler.FluidChange.ORDER_CHANGED, FluidStack.EMPTY);
        if (self.getLevel() != null) {
            self.getLevel().sendBlockUpdated(self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
        }
    }

    public static void handleBreak(HeatingStructureBlockEntity self) {
        if (self.getTank().getCapacity() != 0) {
            self.getTank().setCapacity(0);
            self.setChangedFast();
            if (self.getLevel() != null) {
                self.getLevel().sendBlockUpdated(self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
            }
        }
    }
}