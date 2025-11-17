package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Celestial_Aura extends Modifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    /** 每 tick 根据等级提升百分比回血：初始5%，每级+2% */
    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Level level,
                                @NotNull LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, @NotNull ItemStack stack) {
        if (level.isClientSide || !isCorrectSlot) return;
        if (!(holder instanceof Player player)) return;

        int modLevel = modifier.getLevel();
        if (modLevel <= 0) return;

        // 初始 5%，每级 +2%
        float healPercent = 0.05f + 0.02f * (modLevel - 1);
        float healAmount = player.getMaxHealth() * healPercent;

        // 仅当未满血时回复
        if (player.getHealth() < player.getMaxHealth()) {
            player.heal(healAmount);
        }
    }
}