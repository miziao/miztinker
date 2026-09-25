package com.mizi.miztinker.recipes.rules;

import com.mizi.miztinker.config.MiztinkerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class DeathNoteRule implements ITransformRule {
    private static final ResourceLocation ID = ResourceLocation.parse("miztinker:death_note");
    private static final String ORIGIN_Y_KEY = "miztinker:origin_y";

    @Override
    public boolean isInput(ItemStack stack) {
        if (!MiztinkerConfig.ENABLE_DEATH_NOTE_TRANSFORM.get()) {
            return false;
        }

        return stack.is(Items.WRITABLE_BOOK);
    }

    @Override
    public boolean matches(ItemEntity item, ServerLevel level) {
        if (!MiztinkerConfig.ENABLE_DEATH_NOTE_TRANSFORM.get()) {
            return false;
        }

        double originY = item.getPersistentData().getDouble(ORIGIN_Y_KEY);

        return originY >= 320.0D && item.getY() <= -40.0D;
    }

    @Override
    public ItemStack getResult(ItemStack input, ServerLevel level) {
        var item = ForgeRegistries.ITEMS.getValue(ID);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, input.getCount());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}