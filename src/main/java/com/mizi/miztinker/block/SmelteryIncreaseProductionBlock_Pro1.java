package com.mizi.miztinker.block;


import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;
import slimeknights.tconstruct.smeltery.block.entity.component.SmelteryComponentBlockEntity;

public class SmelteryIncreaseProductionBlock_Pro1 extends SearedBlock {
    public static BlockEntityType<SmelteryIncreaseProductionBlockEntity> BLOCK_ENTITY_TYPE;

    public SmelteryIncreaseProductionBlock_Pro1(Properties properties) {
        super(properties, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (requiredBlockEntity || state.getValue(SearedBlock.IN_STRUCTURE)) {
            return new SmelteryIncreaseProductionBlockEntity(pos, state);
        }
        return null;
    }

    public static class SmelteryIncreaseProductionBlockEntity extends SmelteryComponentBlockEntity {
        public SmelteryIncreaseProductionBlockEntity(BlockPos pos, BlockState state) {
            super(MiztinkerBlocks.SMELTERY_INCREASE_PRODUCTION_PRO1_ENTITY.get(), pos, state);
        }
    }
}