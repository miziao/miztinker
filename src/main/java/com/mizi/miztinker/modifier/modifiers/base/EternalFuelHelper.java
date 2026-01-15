package com.mizi.miztinker.modifier.modifiers.base;

import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class EternalFuelHelper {

    public static boolean isEternalFuelActive(BlockEntity controller) {
        if (controller == null || controller.getLevel() == null) return false;

        Level level = controller.getLevel();
        try {
            Method getStructureMethod = controller.getClass().getMethod("getStructure");
            Object structureObj = getStructureMethod.invoke(controller);

            if (structureObj instanceof StructureData structure) {
                AtomicBoolean hasEternal = new AtomicBoolean(false);
                Block targetBlock = MiztinkerBlocks.ETERNAL_FUEL.get();

                structure.forEachContained(mutablePos -> {
                    if (hasEternal.get()) return;

                    BlockState state = level.getBlockState(mutablePos);

                    if (state.hasProperty(SearedBlock.IN_STRUCTURE) && state.getValue(SearedBlock.IN_STRUCTURE)) {
                        if (state.is(targetBlock)) {
                            hasEternal.set(true);
                        }
                    }
                });
                return hasEternal.get();
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}