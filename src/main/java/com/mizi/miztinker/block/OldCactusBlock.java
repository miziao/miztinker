package com.mizi.miztinker.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OldCactusBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;

    protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
    protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public OldCactusBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    // 2. 实体碰撞伤害逻辑
    @Override
    public void entityInside(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        entity.hurt(level.damageSources().cactus(), 1.0F);
    }

    // 3. 增强生存检查 (禁止并排摆放)
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState neighborState = level.getBlockState(pos.relative(direction));
            if (neighborState.isSolid() || level.getFluidState(pos.relative(direction)).isSource()) {
                return false;
            }
        }
        BlockState belowState = level.getBlockState(pos.below());
        return (belowState.is(Blocks.SAND) || belowState.is(Blocks.RED_SAND) || belowState.is(this));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // 处理被安排的刻逻辑
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        } else {
            BlockPos abovePos = pos.above();
            if (level.isEmptyBlock(abovePos)) {
                int i = state.getValue(AGE);
                if (i == 15) {
                    int height = 1;
                    for(int j = 1; level.getBlockState(pos.below(j)).is(this); ++j) {
                        height++;
                    }
                    if (height < 12) {
                        level.setBlockAndUpdate(abovePos, this.defaultBlockState());
                        level.setBlock(pos, state.setValue(AGE, 0), 4);
                    }
                } else {
                    level.setBlock(pos, state.setValue(AGE, i + 1), 4);
                }
            }
        }
    }



    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}