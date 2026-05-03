package com.mizi.miztinker.block;

import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DynamaxSaplingEntity extends BlockEntity {
    private String saplingType = "minecraft:oak_sapling";

    public DynamaxSaplingEntity(BlockPos pos, BlockState state) {
        super(MiztinkerBlocks.DYNAMAX_SAPLING_ENTITY.get(), pos, state);
    }

    public void setSaplingType(String type) {
        this.saplingType = type;
        this.setChanged();
    }

    public String getSaplingType() {
        return this.saplingType;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString("SaplingType", saplingType);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("SaplingType")) {
            this.saplingType = nbt.getString("SaplingType");
        }
    }
}