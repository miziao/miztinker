package com.mizi.miztinker.modifier.hook;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

public interface LeftClickModifierHook {
    default void onLeftClickEmpty(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot slot) {}

    default void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot slot, BlockState state, BlockPos pos) {}

    class DefaultClass implements LeftClickModifierHook {}

    record AllMerger(Collection<LeftClickModifierHook> modules) implements LeftClickModifierHook {
        @Override
        public void onLeftClickEmpty(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot slot) {
            for (LeftClickModifierHook module : modules) {
                module.onLeftClickEmpty(tool, entry, player, level, slot);
            }
        }

        @Override
        public void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot slot, BlockState state, BlockPos pos) {
            for (LeftClickModifierHook module : modules) {
                module.onLeftClickBlock(tool, entry, player, level, slot, state, pos);
            }
        }
    }
}