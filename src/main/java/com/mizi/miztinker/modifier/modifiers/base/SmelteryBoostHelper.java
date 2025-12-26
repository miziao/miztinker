package com.mizi.miztinker.modifier.modifiers.base;

import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.common.config.Config; // 导入匠魂配置
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SmelteryBoostHelper {

    public static float getSmelteryMultiplier(IMeltingContainer inv) {
        float baseMultiplier = Config.COMMON.repairKitAmount.get().floatValue();
        int pro2Bonus = 0;

        try {
            Object controllerEntity = null;
            try {
                Method getParentMethod = inv.getClass().getMethod("getParent");
                controllerEntity = getParentMethod.invoke(inv);
            } catch (NoSuchMethodException e) {
                Field parentField = inv.getClass().getDeclaredField("parent");
                parentField.setAccessible(true);
                controllerEntity = parentField.get(inv);
            }

            if (controllerEntity instanceof BlockEntity be) {
                Level level = be.getLevel();
                if (level == null) return baseMultiplier;

                Method getStructureMethod = be.getClass().getMethod("getStructure");
                Object structureObj = getStructureMethod.invoke(be);

                if (structureObj instanceof StructureData structure) {
                    AtomicBoolean hasBase = new AtomicBoolean(false);
                    AtomicBoolean hasPro1 = new AtomicBoolean(false);
                    AtomicInteger countPro2 = new AtomicInteger(0);

                    ResourceLocation idBase = MiztinkerBlocks.SMELTERY_INCREASE_PRODUCTION.getId();
                    ResourceLocation idPro1 = MiztinkerBlocks.SMELTERY_INCREASE_PRODUCTION_PRO1.getId();
                    ResourceLocation idPro2 = MiztinkerBlocks.SMELTERY_INCREASE_PRODUCTION_PRO2.getId();

                    structure.forEachContained(mutablePos -> {
                        BlockState state = level.getBlockState(mutablePos);
                        if (state.hasProperty(SearedBlock.IN_STRUCTURE) && state.getValue(SearedBlock.IN_STRUCTURE)) {
                            ResourceLocation currentId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                            if (currentId == null) return;

                            if (currentId.equals(idBase)) {
                                hasBase.set(true);
                            } else if (currentId.equals(idPro1)) {
                                hasPro1.set(true);
                            } else if (currentId.equals(idPro2)) {
                                countPro2.incrementAndGet();
                            }
                        }
                    });

                    if (hasBase.get()) baseMultiplier *= 2.0f;
                    if (hasPro1.get()) baseMultiplier *= 4.0f;

                    pro2Bonus = countPro2.get() * 8;

                    float finalMultiplier = baseMultiplier + pro2Bonus;
                    return Math.min(finalMultiplier, 512.0f);
                }
            }
        } catch (Exception ignored) {
        }

        return baseMultiplier;
    }
}