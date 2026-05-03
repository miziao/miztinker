package com.mizi.miztinker.modifier.hook;


import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.packets.MizLeftClickEmptyPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Collection;

public interface LeftClickModifierHook {
    default void onLeftClickEmpty(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot equipmentSlot) {}

    default void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot equipmentSlot, BlockState state, BlockPos pos) {}

    static void handleLeftClick(ItemStack stack, Player player, EquipmentSlot slot) {
        Level level = player.level();
        if (stack.isEmpty() || !(stack.getItem() instanceof IModifiable)) return;

        IToolStackView tool = ToolStack.from(stack);
        for (ModifierEntry entry : tool.getModifierList()) {
            LeftClickModifierHook hook = entry.getHook(MiztinkerHooks.LEFT_CLICK);
            hook.onLeftClickEmpty(tool, entry, player, level, slot);
        }

        if (level.isClientSide) {
            MiztinkerNetwork.INSTANCE.sendToServer(new MizLeftClickEmptyPacket());
        }
    }

    static void handleLeftClickBlock(ItemStack stack, Player player, EquipmentSlot slot, BlockState state, BlockPos pos) {
        Level level = player.level();
        if (stack.isEmpty() || !(stack.getItem() instanceof IModifiable)) return;

        IToolStackView tool = ToolStack.from(stack);
        for (ModifierEntry entry : tool.getModifierList()) {
            LeftClickModifierHook hook = entry.getHook(MiztinkerHooks.LEFT_CLICK);
            hook.onLeftClickBlock(tool, entry, player, level, slot, state, pos);
        }
    }

    record AllMerger(Collection<LeftClickModifierHook> modules) implements LeftClickModifierHook {
        @Override
        public void onLeftClickEmpty(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot equipmentSlot) {
            for (LeftClickModifierHook module : modules) {
                module.onLeftClickEmpty(tool, entry, player, level, equipmentSlot);
            }
        }

        @Override
        public void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot equipmentSlot, BlockState state, BlockPos pos) {
            for (LeftClickModifierHook module : modules) {
                module.onLeftClickBlock(tool, entry, player, level, equipmentSlot, state, pos);
            }
        }
    }
}