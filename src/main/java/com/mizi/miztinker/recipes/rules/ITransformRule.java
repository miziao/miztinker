package com.mizi.miztinker.recipes.rules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface ITransformRule {

    boolean isInput(ItemStack stack);


    boolean matches(ItemEntity entity, ServerLevel level);


    ItemStack getResult(ItemStack input, ServerLevel level);


    default int getTransformTicks() {
        return 0;
    }

    ResourceLocation getId();
}