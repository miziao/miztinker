package com.mizi.miztinker.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;

import java.math.BigInteger;

public class TinkerElectricityModuleBlock extends SearedBlock {
    public static BlockEntityType<TinkerElectricityModuleBlockEntity> BLOCK_ENTITY_TYPE;

    public TinkerElectricityModuleBlock(Properties properties) {
        super(properties, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TinkerElectricityModuleBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static class TinkerElectricityModuleBlockEntity extends BlockEntity {
        private final AbsoluteInfiniteEnergyStorage energyStorage = new AbsoluteInfiniteEnergyStorage();
        private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);

        public TinkerElectricityModuleBlockEntity(BlockPos pos, BlockState state) {
            super(TinkerElectricityModuleBlock.BLOCK_ENTITY_TYPE, pos, state);
        }

        public void receiveEnergyFromSmeltery(int amount) {
            this.energyStorage.addInternal(BigInteger.valueOf(amount));
            this.pushEnergyToNeighbors();
            this.setChanged();
        }

        private void pushEnergyToNeighbors() {
            if (level == null || level.isClientSide) return;
            for (Direction dir : Direction.values()) {
                BlockEntity target = level.getBlockEntity(worldPosition.relative(dir));
                if (target != null && !(target instanceof TinkerElectricityModuleBlockEntity)) {
                    target.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(handler -> {
                        int toSend = energyStorage.getExtractableAmount();
                        if (toSend > 0 && handler.canReceive()) {
                            int received = handler.receiveEnergy(toSend, false);
                            energyStorage.consumeInternal(BigInteger.valueOf(received));
                        }
                    });
                }
            }
        }

        @Override
        protected void saveAdditional(CompoundTag nbt) {
            super.saveAdditional(nbt);
            nbt.putString("InfiniteEnergyStr", energyStorage.getActualEnergy().toString());
        }

        @Override
        public void load(CompoundTag nbt) {
            super.load(nbt);
            if (nbt.contains("InfiniteEnergyStr")) {
                try {
                    energyStorage.setActualEnergy(new BigInteger(nbt.getString("InfiniteEnergyStr")));
                } catch (NumberFormatException e) {
                    energyStorage.setActualEnergy(BigInteger.ZERO);
                }
            }
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ENERGY) return energyCap.cast();
            return super.getCapability(cap, side);
        }

        @Override
        public void invalidateCaps() {
            super.invalidateCaps();
            energyCap.invalidate();
        }

        private static class AbsoluteInfiniteEnergyStorage implements IEnergyStorage {
            private BigInteger energy = BigInteger.ZERO;

            public void addInternal(BigInteger amount) {
                this.energy = this.energy.add(amount);
            }

            public void consumeInternal(BigInteger amount) {
                this.energy = this.energy.subtract(amount);
                if (this.energy.signum() == -1) this.energy = BigInteger.ZERO;
            }

            public BigInteger getActualEnergy() { return this.energy; }
            public void setActualEnergy(BigInteger value) { this.energy = value; }

            public int getExtractableAmount() {
                if (this.energy.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
                    return Integer.MAX_VALUE;
                }
                return this.energy.intValue();
            }

            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return 0;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                if (!canExtract()) return 0;
                int toExtract = Math.min(maxExtract, getExtractableAmount());
                if (!simulate) {
                    this.energy = this.energy.subtract(BigInteger.valueOf(toExtract));
                }
                return toExtract;
            }

            @Override
            public int getEnergyStored() {
                return getExtractableAmount();
            }

            @Override
            public int getMaxEnergyStored() {
                if (this.energy.signum() <= 0) return 1000;

                int digits = this.energy.toString().length();
                BigInteger dynamicMax = BigInteger.TEN.pow(digits);

                if (dynamicMax.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
                    return Integer.MAX_VALUE;
                }

                return dynamicMax.intValue();
            }

            @Override public boolean canExtract() { return true; }
            @Override public boolean canReceive() { return false; }
        }
    }
}