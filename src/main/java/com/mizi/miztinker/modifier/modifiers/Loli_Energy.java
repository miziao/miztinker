package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.lang.reflect.Field;

public class Loli_Energy extends NoLevelsModifier implements InventoryTickModifierHook {

    private static Field maxReceiveField;

    static {
        try {
            maxReceiveField = EnergyStorage.class.getDeclaredField("maxReceive");
            maxReceiveField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && isCorrectSlot && (isSelected || holder.getOffhandItem() == stack)) {
            fillBlockUnder(world, holder);
            if (holder instanceof Player player) {
                fillPlayerInventory(player);
            }
        }
    }

    private void fillBlockUnder(Level world, LivingEntity holder) {
        BlockPos posUnder = holder.blockPosition().below();
        BlockEntity be = world.getBlockEntity(posUnder);
        if (be != null) {
            be.getCapability(ForgeCapabilities.ENERGY, Direction.UP).ifPresent(this::instantForceFill);
            be.setChanged();
        }
    }

    private void fillPlayerInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.isEmpty()) continue;

            invStack.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
                this.instantForceFill(storage);
                if (storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                    writeEnergyToNBT(invStack, storage.getMaxEnergyStored());
                }
            });
        }
    }

    private void instantForceFill(IEnergyStorage storage) {
        if (!storage.canReceive()) return;

        boolean reflectionSuccess = false;

        if (maxReceiveField != null && storage instanceof EnergyStorage) {
            try {
                int originalMax = maxReceiveField.getInt(storage);
                maxReceiveField.setInt(storage, Integer.MAX_VALUE);

                int missing = storage.getMaxEnergyStored() - storage.getEnergyStored();
                storage.receiveEnergy(missing, false);

                maxReceiveField.setInt(storage, originalMax);
                reflectionSuccess = true;
            } catch (Exception ignored) {
            }
        }

        if (!reflectionSuccess) {
            storage.receiveEnergy(Integer.MAX_VALUE, false);
        }
    }

    private void writeEnergyToNBT(ItemStack stack, int amount) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt("Energy", amount);
        nbt.putInt("energy", amount);
        if (nbt.contains("BlockEntityTag", 10)) {
            nbt.getCompound("BlockEntityTag").putInt("Energy", amount);
        }
    }
}