package com.mizi.miztinker.recipes.rules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class StormBookRule implements ITransformRule {
    private static final ResourceLocation ID = ResourceLocation.parse("miztinker:born_of_the_storm");

    @Override
    public boolean isInput(ItemStack stack) {
        return stack.is(Items.BOOK);
    }

    @Override
    public boolean matches(ItemEntity item, ServerLevel level) {
        return (level.isThundering() || level.isRaining()) && level.canSeeSky(item.blockPosition());
    }

    @Override
    public int getTransformTicks() {
        return 1200;
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