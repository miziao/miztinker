package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.util.EnergyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Loli_Energy extends NoLevelsModifier implements BlockInteractionModifierHook, InventoryTickModifierHook {

    private static final String KEY_SUCCESS = "modifier.miztinker.loli_energy.success";
    private static final String KEY_FAILURE = "modifier.miztinker.loli_energy.failure";

    @Override
    protected void registerHooks(ModuleHookMap.@NotNull Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public @NotNull InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        Level world = context.getLevel();
        Player player = context.getPlayer();

        if (world.isClientSide || source != InteractionSource.RIGHT_CLICK) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        BlockEntity be = world.getBlockEntity(pos);

        if (be != null) {
            System.out.println("[LoliEnergy] Attempting to charge: " + be.getClass().getSimpleName() + " at " + pos);

            if (EnergyManager.fillEnergyCompletely(be)) {
                be.setChanged();
                world.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3);

                if (player != null) {
                    player.sendSystemMessage(Component.translatable(KEY_SUCCESS, be.getClass().getSimpleName()));
                }
                return InteractionResult.SUCCESS;
            } else {
                if (player != null) {
                    player.sendSystemMessage(Component.translatable(KEY_FAILURE));
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !isCorrectSlot || holder.tickCount % 20 != 0) return;

        if (holder instanceof Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (!invStack.isEmpty() && invStack != stack) {
                    EnergyManager.chargeEnergy(invStack, Long.MAX_VALUE);
                }
            }

            BlockEntity beBelow = level.getBlockEntity(holder.blockPosition().below());
            if (beBelow != null) {
                EnergyManager.fillEnergyCompletely(beBelow);
            }
        }
    }
}