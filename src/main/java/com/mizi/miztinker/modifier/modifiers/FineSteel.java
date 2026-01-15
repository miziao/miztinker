package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.RepairFactorModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public class FineSteel extends Modifier implements RepairFactorModifierHook, ToolStatsModifierHook, TooltipModifierHook {

    private static final ResourceLocation REPAIR_COUNT_KEY = new ResourceLocation("miztinker", "fine_steel_count");
    private static final float BONUS_PER_REPAIR = 0.10f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.REPAIR_FACTOR);
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public float getRepairFactor(IToolStackView tool, ModifierEntry entry, float factor) {
        if (tool.getDamage() > 0) {
            float currentBonus = tool.getPersistentData().getFloat(REPAIR_COUNT_KEY);
            float maxBonus = (float) entry.getLevel();

            if (currentBonus < maxBonus) {
                float nextBonus = Math.min(maxBonus, currentBonus + BONUS_PER_REPAIR);
                tool.getPersistentData().putFloat(REPAIR_COUNT_KEY, nextBonus);
            }
        }
        return factor;
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry entry, ModifierStatsBuilder builder) {
        float bonus = context.getPersistentData().getFloat(REPAIR_COUNT_KEY);
        if (bonus > 0) {
            ToolStats.ATTACK_DAMAGE.add(builder, bonus);
            ToolStats.DURABILITY.multiply(builder, 1.0f + bonus);
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry entry, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        float bonus = tool.getPersistentData().getFloat(REPAIR_COUNT_KEY);
        float maxBonus = (float) entry.getLevel();

        if (bonus > 0) {
            int percentage = (int) (bonus * 100);
            int maxPercentage = (int) (maxBonus * 100);

            tooltip.add(Component.translatable("modifier.miztinker.fine_steel.bonus")
                    .append(Component.literal(": " + percentage + "% / " + maxPercentage + "%"))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}