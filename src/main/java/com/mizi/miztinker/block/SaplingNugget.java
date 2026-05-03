package com.mizi.miztinker.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import org.jetbrains.annotations.Nullable;

public class SaplingNugget extends SaplingBlock {

    public SaplingNugget(Properties properties) {
        super(new AbstractTreeGrower() {
            @Nullable @Override
            protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> getConfiguredFeature(net.minecraft.util.RandomSource random, boolean hasFlowers) { return null; }
        }, properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) return;

        String saplingId = "minecraft:oak_sapling";
        if (stack.hasTag() && stack.getTag().contains("SaplingType")) {
            saplingId = stack.getTag().getString("SaplingType");
        }

        Block log = autoFindBlock(saplingId, "_log", "_stem", Blocks.OAK_LOG);
        Block leaves = autoFindBlock(saplingId, "_leaves", "_wart_block", Blocks.OAK_LEAVES);

        level.setBlock(pos, log.defaultBlockState(), 3);

        BlockPos leafPos = pos.above();
        if (level.getBlockState(leafPos).isAir() || level.getBlockState(leafPos).canBeReplaced()) {
            BlockState leafState = leaves.defaultBlockState();
            if (leafState.hasProperty(LeavesBlock.PERSISTENT)) {
                leafState = leafState.setValue(LeavesBlock.PERSISTENT, true);
            }
            level.setBlock(leafPos, leafState, 3);
        }
    }

    private Block autoFindBlock(String saplingId, String primarySuffix, String secondarySuffix, Block fallback) {
        String baseId = saplingId.replaceAll("(_sapling|_propagule|_fungus|_sprouts)$", "");

        ResourceLocation primaryRL = ResourceLocation.parse(baseId + primarySuffix);
        if (BuiltInRegistries.BLOCK.containsKey(primaryRL)) {
            return BuiltInRegistries.BLOCK.get(primaryRL);
        }

        ResourceLocation secondaryRL = ResourceLocation.parse(baseId + secondarySuffix);
        if (BuiltInRegistries.BLOCK.containsKey(secondaryRL)) {
            return BuiltInRegistries.BLOCK.get(secondaryRL);
        }

        return fallback;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {}
}