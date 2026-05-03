package com.mizi.miztinker.recipes.rules.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockInteractRule {

    boolean matches(BlockState state, ResourceLocation blockId, ItemStack heldStack, ResourceLocation heldId);


    void execute(Player player, Level level, BlockPos pos, ItemStack heldStack);


    ResourceLocation getId();
}