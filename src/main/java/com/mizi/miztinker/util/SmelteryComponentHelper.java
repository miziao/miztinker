package com.mizi.miztinker.util;

import com.mizi.miztinker.block.NutrientSolutionModuleBlock;
import com.mizi.miztinker.block.ScorchedSeparatorBlock;
import com.mizi.miztinker.block.SearedAlloyRetarderBlock;
import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class SmelteryComponentHelper {


    public static int getReinforcedBrickCount(BlockEntity controller) {
        if (controller == null || controller.getLevel() == null) return 0;

        StructureData structure = getStructure(controller);
        if (structure == null) return 0;

        Level level = controller.getLevel();
        AtomicInteger count = new AtomicInteger(0);

        structure.forEachContained(mutablePos -> {
            BlockState state = level.getBlockState(mutablePos);
            if (state.is(MiztinkerBlocks.REINFORCED_SEARED_BRICK.get())) {
                count.incrementAndGet();
            }
        });
        return count.get();
    }

    public static float getProductionMultiplier(BlockEntity controller) {
        float baseMultiplier = Config.COMMON.repairKitAmount.get().floatValue();
        if (controller == null || controller.getLevel() == null) return baseMultiplier;

        Level level = controller.getLevel();
        StructureData structure = getStructure(controller);
        if (structure == null) return baseMultiplier;

        AtomicBoolean hasBase = new AtomicBoolean(false);
        AtomicBoolean hasPro1 = new AtomicBoolean(false);
        AtomicInteger countPro2 = new AtomicInteger(0);

        structure.forEachContained(mutablePos -> {
            BlockState state = level.getBlockState(mutablePos);
            if (state.hasProperty(SearedBlock.IN_STRUCTURE) && state.getValue(SearedBlock.IN_STRUCTURE)) {
                Block block = state.getBlock();
                if (block == MiztinkerBlocks.SMELTERY_INCREASE_PRODUCTION.get()) hasBase.set(true);
                else if (block == MiztinkerBlocks.SMELTERY_INCREASE_PRODUCTION_PRO1.get()) hasPro1.set(true);
                else if (block == MiztinkerBlocks.SMELTERY_INCREASE_PRODUCTION_PRO2.get()) countPro2.incrementAndGet();
            }
        });

        float currentMultiplier = baseMultiplier;
        if (hasBase.get()) currentMultiplier *= 2.0f;
        if (hasPro1.get()) currentMultiplier *= 4.0f;

        float finalValue = currentMultiplier + (countPro2.get() * 8.0f);
        return Math.min(finalValue, 512.0f);
    }

    public static boolean isSeparatorActive(BlockEntity controller) {
        return checkComponent(controller, MiztinkerBlocks.SCORCHED_SEPARATOR.get(),
                state -> state.getValue(ScorchedSeparatorBlock.POWERED));
    }

    public static boolean isNutrientModuleActive(BlockEntity controller) {
        return checkComponent(controller, MiztinkerBlocks.NUTRIENT_SOLUTION_MODULE.get(),
                state -> state.hasProperty(NutrientSolutionModuleBlock.POWERED)
                        && state.getValue(NutrientSolutionModuleBlock.POWERED));
    }

    public static boolean isRetarderActive(BlockEntity controller) {
        return checkComponent(controller, MiztinkerBlocks.SearedAlloyRetarder.get(),
                state -> state.getValue(SearedAlloyRetarderBlock.POWERED));
    }

    public static boolean isEternalFuelActive(BlockEntity controller) {
        return checkComponent(controller, MiztinkerBlocks.ETERNAL_FUEL.get(), state -> true);
    }


    private static StructureData getStructure(BlockEntity controller) {
        try {
            Method getStructureMethod = controller.getClass().getMethod("getStructure");
            Object structureObj = getStructureMethod.invoke(controller);
            if (structureObj instanceof StructureData) return (StructureData) structureObj;
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean checkComponent(BlockEntity controller, Block targetBlock, Predicate<BlockState> extraCheck) {
        StructureData structure = getStructure(controller);
        if (structure == null || controller.getLevel() == null) return false;

        Level level = controller.getLevel();
        AtomicBoolean found = new AtomicBoolean(false);

        structure.forEachContained(mutablePos -> {
            if (found.get()) return;
            BlockState state = level.getBlockState(mutablePos);
            if (state.is(targetBlock) && state.hasProperty(SearedBlock.IN_STRUCTURE) && state.getValue(SearedBlock.IN_STRUCTURE)) {
                if (extraCheck.test(state)) found.set(true);
            }
        });
        return found.get();
    }
}