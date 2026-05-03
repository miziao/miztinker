package com.mizi.miztinker.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockDynamaxSapling extends BushBlock implements BonemealableBlock, EntityBlock {

    public static final List<TreeGeneratorTask> ACTIVE_TASKS = new ArrayList<>();

    public BlockDynamaxSapling() {
        super(BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DynamaxSaplingEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && stack.hasTag() && stack.getTag().contains("SaplingType")) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DynamaxSaplingEntity dynamaxBE) {
                dynamaxBE.setSaplingType(stack.getTag().getString("SaplingType"));
            }
        }
    }

    public void performGrowth(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        String type = "minecraft:oak_sapling";
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DynamaxSaplingEntity dynamaxBE) {
            type = dynamaxBE.getSaplingType();
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        ACTIVE_TASKS.add(new TreeGeneratorTask(level, pos, 320, type));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, net.minecraft.util.RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.45D;
    }

    @Override
    public void performBonemeal(ServerLevel level, net.minecraft.util.RandomSource random, BlockPos pos, BlockState state) {
        this.performGrowth(level, random, pos, state);
    }

    @Mod.EventBusSubscriber(modid = "miztinker")
    public static class EventHandler {
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Iterator<TreeGeneratorTask> it = ACTIVE_TASKS.iterator();
                while (it.hasNext()) {
                    TreeGeneratorTask task = it.next();
                    if (task.isFinished()) {
                        it.remove();
                    } else {
                        task.runTick();
                    }
                }
            }
        }
    }
}