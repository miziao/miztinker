package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class WaterInjectedPork extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && isCorrectSlot && (isSelected || holder.getOffhandItem() == stack)) {
            if (holder.tickCount % 20 == 0) {
                injectFluidUnder(world, holder, tool);
            }
        }
    }

    private void injectFluidUnder(Level world, LivingEntity holder, IToolStackView tool) {
        CompoundTag persistentData = tool.getPersistentData().getCompound(ResourceLocation.parse("tconstruct:tank_fluid"));

        if (persistentData.isEmpty() || !persistentData.getString("FluidName").equals("minecraft:water")) {
            return;
        }

        int currentWater = persistentData.getInt("Amount");
        if (currentWater <= 0) return;

        float capacity = tool.getVolatileData().getFloat(ResourceLocation.parse("tconstruct:tank_capacity"));
        if (capacity <= 0) capacity = 1000f;
        int drainAmount = (int) Math.ceil(capacity * 0.01);

        BlockPos posUnder = holder.blockPosition().below();
        BlockEntity be = world.getBlockEntity(posUnder);

        if (be != null) {
            be.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).ifPresent(handler -> {

                FluidStack targetFluid = FluidStack.EMPTY;
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fluidInTank = handler.getFluidInTank(i);
                    if (!fluidInTank.isEmpty()) {
                        targetFluid = fluidInTank.copy();
                        break;
                    }
                }

                if (targetFluid.isEmpty()) {
                    return;
                }

                FluidStack toFill = new FluidStack(targetFluid.getFluid(), drainAmount);
                int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);

                if (filled > 0) {
                    int nextWater = Math.max(0, currentWater - drainAmount);
                    if (nextWater == 0) {
                        tool.getPersistentData().remove(ResourceLocation.parse("tconstruct:tank_fluid"));
                    } else {
                        persistentData.putInt("Amount", nextWater);
                    }
                    be.setChanged();
                }
            });
        }
    }
}