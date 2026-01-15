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

public class EternalFuelBlock extends SearedBlock {
    public static BlockEntityType<EternalFuelBlockEntity> BLOCK_ENTITY_TYPE;

    public EternalFuelBlock(Properties properties) {
        super(properties, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (state.getValue(SearedBlock.IN_STRUCTURE)) {
            return new EternalFuelBlockEntity(pos, state);
        }
        return null;
    }

    public static class EternalFuelBlockEntity extends SmelteryComponentBlockEntity {
        public EternalFuelBlockEntity(BlockPos pos, BlockState state) {
            super(MiztinkerBlocks.ETERNAL_FUEL_ENTITY.get(), pos, state);
        }
    }
}