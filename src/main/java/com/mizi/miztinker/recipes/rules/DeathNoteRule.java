package com.mizi.miztinker.recipes.rules;

import com.mizi.miztinker.config.MiztinkerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class DeathNoteRule implements ITransformRule {
    private static final ResourceLocation ID = ResourceLocation.parse("miztinker:death_note");

    @Override
    public boolean isInput(ItemStack stack) {
        if (!MiztinkerConfig.ENABLE_DEATH_NOTE_TRANSFORM.get()) return false;
        return stack.is(Items.WRITABLE_BOOK);
    }

    @Override
    public boolean matches(ItemEntity item, ServerLevel level) {
        if (!MiztinkerConfig.ENABLE_DEATH_NOTE_TRANSFORM.get()) return false;

        double originY = item.getPersistentData().getDouble("miztinker:origin_y");
        return originY >= 320 && item.getY() <= -40;
    }

    @Override
    public ItemStack getResult(ItemStack input, ServerLevel level) {
        return new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ID)), input.getCount());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}