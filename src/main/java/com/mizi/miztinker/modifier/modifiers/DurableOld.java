package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.DisplayNameModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class DurableOld extends Modifier implements ToolDamageModifierHook, InventoryTickModifierHook, DisplayNameModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE, ModifierHooks.INVENTORY_TICK, ModifierHooks.DISPLAY_NAME);
    }

    @Override
    public Component getDisplayName(int level) {
        if (level >= 5) {
            return Component.translatable("modifier.miztinker.unbreakable")
                    .withStyle(style -> style.withColor(0xFFD700));
        }
        return super.getDisplayName(level);
    }

    @Override
    public @NotNull Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
        int level = entry.getLevel();
        if (level >= 5) {
            return Component.translatable("modifier.miztinker.unbreakable")
                    .withStyle(style -> style.withColor(0xFFD700));
        }
        return name;
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry entry, int amount, @Nullable LivingEntity holder) {
        int level = entry.getLevel();
        if (level >= 5) return 0;

        if (RANDOM.nextFloat() < (level * 0.2f)) {
            return 0;
        }
        return amount;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (entry.getLevel() >= 5) {
            if (tool.getDamage() > 0) {
                tool.setDamage(0);
            }
        }
    }
}