package com.mizi.miztinker.recipes.rules.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class MendingMossRule implements IBlockInteractRule {
    private static final ResourceLocation ID = ResourceLocation.parse("miztinker:mending_moss_recipe");

    @Override
    public boolean matches(BlockState state, ResourceLocation blockId, ItemStack heldStack, ResourceLocation heldId) {
        return ResourceLocation.parse("miztinker:moss").equals(heldId) &&
                ResourceLocation.parse("minecraft:bookshelf").equals(blockId);
    }

    @Override
    public void execute(Player player, Level level, BlockPos pos, ItemStack heldStack) {
        if (player.experienceLevel < 10) {
            if (!level.isClientSide) player.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        player.giveExperienceLevels(-10);

        var itemRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ITEM);
        ItemStack result = new ItemStack(Objects.requireNonNull(itemRegistry.get(ResourceLocation.parse("miztinker:mending_moss"))), 1);

        heldStack.shrink(1);

        if (!player.getInventory().add(result) && level instanceof ServerLevel sl) {
            sl.addFreshEntity(new ItemEntity(sl, player.getX(), player.getY(), player.getZ(), result));
        }

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);
            sl.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 0.8f);
        }
    }

    @Override
    public ResourceLocation getId() { return ID; }
}