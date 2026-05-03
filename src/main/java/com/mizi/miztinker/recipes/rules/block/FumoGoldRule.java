package com.mizi.miztinker.recipes.rules.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class FumoGoldRule implements IBlockInteractRule {
    private static final ResourceLocation ID = ResourceLocation.parse("miztinker:fumo_gold_recipe");

    @Override
    public boolean matches(BlockState state, ResourceLocation blockId, ItemStack heldStack, ResourceLocation heldId) {
        return ResourceLocation.parse("minecraft:gold_ingot").equals(heldId) &&
                ResourceLocation.parse("minecraft:bookshelf").equals(blockId);
    }

    @Override
    public void execute(Player player, Level level, BlockPos pos, ItemStack heldStack) {
        var itemRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ITEM);
        ItemStack result = new ItemStack(Objects.requireNonNull(itemRegistry.get(ResourceLocation.parse("miztinker:fumo_gold_ingot"))), heldStack.getCount());

        if (!player.getInventory().add(result) && level instanceof ServerLevel sl) {
            sl.addFreshEntity(new ItemEntity(sl, player.getX(), player.getY(), player.getZ(), result));
        }

        heldStack.shrink(heldStack.getCount());

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1, player.getZ(), 10, 0.25, 0.25, 0.25, 0.01);
        }
    }

    @Override
    public ResourceLocation getId() { return ID; }
}