package com.mizi.miztinker.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;

public class ScorchedSeparatorBlock extends Block {
    public static final BooleanProperty IN_STRUCTURE = SearedBlock.IN_STRUCTURE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ScorchedSeparatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(IN_STRUCTURE, false)
                .setValue(POWERED, false));
    }


    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {

            boolean hasSignal = level.hasNeighborSignal(pos);

            if (state.getValue(POWERED) != hasSignal) {
                level.setBlock(pos, state.setValue(POWERED, hasSignal), 2);

            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IN_STRUCTURE, POWERED);
    }
}