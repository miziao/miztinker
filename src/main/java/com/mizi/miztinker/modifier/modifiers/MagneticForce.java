package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.tools.nbt.*;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.fluids.TinkerFluids;

public class MagneticForce extends Modifier implements ToolStatsModifierHook, InventoryTickModifierHook {

    private static final ResourceLocation ABSORBED_MB = ResourceLocation.fromNamespaceAndPath("mizi", "magnetic_absorbed_mb");
    private static final int MB_PER_INGOT = 90;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!level.isClientSide && holder instanceof Player player && holder.tickCount % 20 == 0 && (isSelected || isCorrectSlot)) {
            boolean changed = false;
            ModDataNBT persistentData = tool.getPersistentData();
            long currentMb = persistentData.get(ABSORBED_MB, CompoundTag::getLong);

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);

                if (invStack.isEmpty() || invStack == stack) continue;

                int fluidAmount = getIronAmount(level, invStack);
                if (fluidAmount > 0) {
                    currentMb += (long) invStack.getCount() * fluidAmount;
                    invStack.setCount(0);
                    changed = true;
                    break;
                }
            }

            if (changed) {
                persistentData.put(ABSORBED_MB, LongTag.valueOf(currentMb));

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (tool instanceof ToolStack toolStack) {
                    toolStack.rebuildStats();
                }
            }
        }
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        long totalMb = context.getPersistentData().get(ABSORBED_MB, CompoundTag::getLong);
        if (totalMb <= 0) return;

        double ingots = (double) totalMb / MB_PER_INGOT;
        int level = modifier.getLevel();

        double baseRate = level * 0.01;
        float multiplier;

        if (level < 5) {
            multiplier = (float) (1.0 + (ingots * baseRate));
        } else {
            multiplier = (float) Math.pow(1.0 + baseRate, ingots);
        }

        applyToAll(builder, multiplier);
    }

    private void applyToAll(ModifierStatsBuilder builder, float rate) {
        ToolStats.DURABILITY.multiply(builder, rate);
        ToolStats.ATTACK_DAMAGE.multiply(builder, rate);
        ToolStats.ATTACK_SPEED.multiply(builder, rate);
        ToolStats.MINING_SPEED.multiply(builder, rate);
        ToolStats.ARMOR.multiply(builder, rate);
        ToolStats.ARMOR_TOUGHNESS.multiply(builder, rate);
        ToolStats.DRAW_SPEED.multiply(builder, rate);
        ToolStats.VELOCITY.multiply(builder, rate);
        ToolStats.PROJECTILE_DAMAGE.multiply(builder, rate);
    }


    private int getIronAmount(Level level, ItemStack stack) {
        MeltingContainer container = new MeltingContainer(stack);
        return level.getRecipeManager()
                .getRecipeFor(TinkerRecipeTypes.MELTING.get(), container, level)
                .map(recipe -> {
                    FluidStack result = recipe.getOutput(container);
                    if (result.getFluid().isSame(TinkerFluids.moltenIron.get())) {
                        return result.getAmount();
                    }
                    return 0;
                }).orElse(0);
    }

    private record MeltingContainer(ItemStack stack) implements IMeltingContainer, IMeltingContainer.IOreRate {
        @Override public @NotNull ItemStack getStack() { return stack; }
        @Override public @NotNull IOreRate getOreRate() { return this; }
        @Override public int applyOreBoost(OreRateType rateType, int amount) { return amount; }
    }
}