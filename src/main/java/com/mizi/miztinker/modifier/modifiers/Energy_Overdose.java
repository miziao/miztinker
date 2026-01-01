package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.DurabilityManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class Energy_Overdose extends NoLevelsModifier implements ToolDamageModifierHook, InventoryTickModifierHook {


    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry entry, int amount, @Nullable LivingEntity holder) {
        return amount;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide) return;

        long theoreticalMax = DurabilityManager.getTheoreticalMax(tool);
        int actualMax = tool.getStats().getInt(ToolStats.DURABILITY);

        if (theoreticalMax > actualMax) {
            long diff = theoreticalMax - actualMax;
            int currentDamage = tool.getDamage();

            if (currentDamage > 0) {
                int repair = (int) Math.min(currentDamage, diff);
                tool.setDamage(currentDamage - repair);
            }
        }
    }
    }
