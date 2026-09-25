package com.mizi.miztinker.recipes.rules;

import net.minecraft.core.particles.ParticleTypes;
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
        return !level.isDay()
                && item.getY() > 300.0D
                && level.canSeeSky(item.blockPosition());
    }

    @Override
    public int getTransformTicks() {
        return 600;
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

    @Override
    public void onTransforming(ItemEntity item, ServerLevel level, int elapsedTicks, int requiredTicks) {
        if (elapsedTicks % 5 != 0) {
            return;
        }

        level.sendParticles(
                ParticleTypes.ENCHANT,
                item.getX(),
                item.getY() + 0.5D,
                item.getZ(),
                4,
                0.45D,
                0.45D,
                0.45D,
                0.03D
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                item.getX(),
                item.getY() + 0.35D,
                item.getZ(),
                1,
                0.15D,
                0.15D,
                0.15D,
                0.01D
        );
    }
}