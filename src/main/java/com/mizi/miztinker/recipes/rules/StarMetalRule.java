package com.mizi.miztinker.recipes.rules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class StarMetalRule implements ITransformRule {
    private static final ResourceLocation ID = ResourceLocation.parse("miztinker:starmetal_ingot");

    @Override
    public boolean isInput(ItemStack stack) {
        return stack.is(Items.IRON_INGOT);
    }

    @Override
    public boolean matches(ItemEntity item, ServerLevel level) {
        return !level.isDay() && item.getY() > 300;
    }

    @Override
    public int getTransformTicks() {
        return 2400; // 120秒
    }

    @Override
    public ItemStack getResult(ItemStack input, ServerLevel level) {
        var item = ForgeRegistries.ITEMS.getValue(ID);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, input.getCount());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}