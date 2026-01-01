package com.mizi.miztinker.block;

import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.ArrayList;
import java.util.List;

public class TinkerLanternBlockEntity extends BlockEntity {
    private final List<BlockPos> cachedTargets = new ArrayList<>();
    private int scanTimer = 0;
    private static final int ACCELERATION_FACTOR = 200;

    // 新增：红石控制标志位
    private boolean disabledByRedstone = false;

    public TinkerLanternBlockEntity(BlockPos pos, BlockState state) {
        super(MiztinkerBlocks.TINKER_LANTERN_BE.get(), pos, state);
    }

    // 提供给 Block 类调用的方法
    public void setDisabledByRedstone(boolean disabled) {
        this.disabledByRedstone = disabled;
        this.setChanged(); // 标记数据已改变
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TinkerLanternBlockEntity be) {
        // 如果被红石激活（通电），则跳过加速逻辑
        if (be.disabledByRedstone) {
            return;
        }

        if (be.scanTimer-- <= 0) {
            be.scanTimer = 20;
            be.refreshTargets(level, pos);
        }

        if (!be.cachedTargets.isEmpty()) {
            for (BlockPos targetPos : be.cachedTargets) {
                BlockEntity targetBE = level.getBlockEntity(targetPos);
                if (targetBE != null && targetBE.getType() != MiztinkerBlocks.TINKER_LANTERN_BE.get()) {
                    accelerateBlockEntity(level, targetPos, targetBE);
                }
            }
        }
    }

    private void refreshTargets(Level level, BlockPos pos) {
        cachedTargets.clear();
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-9, -2, -9), pos.offset(9, 2, 9))) {
            if (checkPos.equals(pos)) continue;

            BlockState state = level.getBlockState(checkPos);
            if (isTinkerTarget(state)) {
                cachedTargets.add(checkPos.immutable());
            }
        }
    }

    private static boolean isTinkerTarget(BlockState state) {
        Block block = state.getBlock();
        if (block == MiztinkerBlocks.TINKER_LANTERN.get()) {
            return false;
        }
        return state.is(TinkerTags.Blocks.SEARED_BLOCKS) ||
                state.is(TinkerTags.Blocks.SCORCHED_BLOCKS) ||
                block == TinkerSmeltery.searedFaucet.get() ||
                block == TinkerSmeltery.scorchedFaucet.get() ||
                block == TinkerSmeltery.searedBasin.get() ||
                block == TinkerSmeltery.searedTable.get();
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> void accelerateBlockEntity(Level level, BlockPos pos, T be) {
        if (be.getType() == MiztinkerBlocks.TINKER_LANTERN_BE.get()) {
            return;
        }
        BlockState state = be.getBlockState();
        BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) state.getTicker(level, be.getType());
        if (ticker != null) {
            for (int i = 0; i < ACCELERATION_FACTOR; i++) {
                ticker.tick(level, pos, state, be);
            }
        }
    }

    // 必须重写持久化方法，否则存档重启后红石状态会丢失
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.disabledByRedstone = tag.getBoolean("disabled");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("disabled", this.disabledByRedstone);
    }
}