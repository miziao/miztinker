package com.mizi.miztinker.modifier.modifiers.base;


import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class SmelteryBoostHelper {
    public static boolean hasBoostBlockInStructure(IMeltingContainer inv) {
        String prefix = ">>>> [MIZTINKER_DEBUG] ";
        try {
            Method getTileMethod = inv.getClass().getMethod("getTile");
            Object tile = getTileMethod.invoke(inv);

            if (tile instanceof BlockEntity be) {
                Level level = be.getLevel();
                if (level == null) return false;

                Method getStructureMethod = tile.getClass().getMethod("getStructure");
                Object structureObj = getStructureMethod.invoke(tile);

                if (structureObj instanceof StructureData structure) {
                    final AtomicBoolean found = new AtomicBoolean(false);

                    // 获取你的方块的 ID：例如 "miztinker:smeltery_increase_production"
                    ResourceLocation targetId = MiztinkerBlocks.SMELTERY_INCREASE_PRODUCTION.getId();

                    System.out.println(prefix + "开始遍历结构。目标 ID: " + targetId);

                    structure.forEachContained(mutablePos -> {
                        if (found.get()) return;

                        BlockState state = level.getBlockState(mutablePos);
                        Block block = state.getBlock();

                        ResourceLocation currentId = ForgeRegistries.BLOCKS.getKey(block);

                        if (targetId.equals(currentId)) {
                            if (state.hasProperty(SearedBlock.IN_STRUCTURE) && state.getValue(SearedBlock.IN_STRUCTURE)) {
                                System.out.println(prefix + "!!! [发现匹配] !!! 坐标: " + mutablePos.immutable());
                                found.set(true);
                            } else {
                                System.out.println(prefix + "找到 ID 匹配但 IN_STRUCTURE 为 false 的方块: " + mutablePos.immutable());
                            }
                        }
                    });

                    if (!found.get()) {
                        System.out.println(prefix + "遍历了所有坐标，未找到 ID 匹配且在结构内的方块。");
                    }

                    return found.get();
                }
            }
        } catch (Exception e) {
            System.out.println(prefix + "反射或执行出错: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}