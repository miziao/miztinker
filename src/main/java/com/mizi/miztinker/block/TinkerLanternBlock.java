package com.mizi.miztinker.block;


import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public class TinkerLanternBlock extends LanternBlock implements EntityBlock {

    public TinkerLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HANGING, false)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TinkerLanternBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == MiztinkerBlocks.TINKER_LANTERN_BE.get()
                ? (level1, pos1, state1, be) -> TinkerLanternBlockEntity.serverTick(level1, pos1, state1, (TinkerLanternBlockEntity) be)
                : null;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.IGNORE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean boolean_1) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, boolean_1);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TinkerLanternBlockEntity lanternBE) {
            boolean isHanging = state.getValue(BlockStateProperties.HANGING);
            boolean powered;
            if (isHanging) {
                powered = level.hasSignal(pos.above(), Direction.UP);
            } else {
                powered = level.hasSignal(pos.north(), Direction.NORTH) ||
                        level.hasSignal(pos.south(), Direction.SOUTH) ||
                        level.hasSignal(pos.west(), Direction.WEST) ||
                        level.hasSignal(pos.east(), Direction.EAST);
            }
            lanternBE.setDisabledByRedstone(powered);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState newState, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(newState, level, pos, oldState, isMoving);

        this.neighborChanged(newState, level, pos, null, null, false);
    }
}