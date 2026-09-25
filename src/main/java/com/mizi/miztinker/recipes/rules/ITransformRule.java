package com.mizi.miztinker.recipes.rules;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    default void onTransforming(ItemEntity item, ServerLevel level, int elapsedTicks, int requiredTicks) {
        if (elapsedTicks % 5 != 0) {
            return;
        }

        level.sendParticles(
                ParticleTypes.ENCHANT,
                item.getX(),
                item.getY() + 0.5D,
                item.getZ(),
                3,
                0.35D,
                0.35D,
                0.35D,
                0.02D
        );
    }

    default void onTransformed(ItemEntity item, ServerLevel level, ItemStack result) {
        level.sendParticles(
                ParticleTypes.ENCHANT,
                item.getX(),
                item.getY() + 0.5D,
                item.getZ(),
                24,
                0.45D,
                0.45D,
                0.45D,
                0.08D
        );

        level.playSound(
                null,
                item.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS,
                1.0F,
                1.2F
        );
    }
}