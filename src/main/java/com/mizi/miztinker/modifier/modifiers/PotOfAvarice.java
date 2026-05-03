package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.EnchantmentModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.LootingModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.LootingContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class PotOfAvarice extends NoLevelsModifier implements
        SlotStackModifierHook, TooltipModifierHook, EnchantmentModifierHook,
        MeleeHitModifierHook, BlockBreakModifierHook, LootingModifierHook {

    private static final ResourceLocation AVARICE_PROGRESS = ResourceLocation.fromNamespaceAndPath("miztinker", "avarice_progress");
    private static final ResourceLocation AVARICE_LEVEL = ResourceLocation.fromNamespaceAndPath("miztinker", "avarice_level");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK, ModifierHooks.TOOLTIP, ModifierHooks.ENCHANTMENTS);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK, ModifierHooks.WEAPON_LOOTING);
    }

    @Override
    public int updateEnchantmentLevel(IToolStackView tool, ModifierEntry modifier, Enchantment enchantment, int level) {
        int avariceLevel = tool.getPersistentData().getInt(AVARICE_LEVEL);
        if (avariceLevel > 0) {
            if (enchantment == Enchantments.BLOCK_FORTUNE || enchantment == Enchantments.MOB_LOOTING) {
                return level + avariceLevel;
            }
        }
        return level;
    }

    @Override
    public void updateEnchantments(IToolStackView tool, ModifierEntry modifier, Map<Enchantment, Integer> enchantments) {
        int avariceLevel = tool.getPersistentData().getInt(AVARICE_LEVEL);
        if (avariceLevel > 0) {
            enchantments.put(Enchantments.BLOCK_FORTUNE, enchantments.getOrDefault(Enchantments.BLOCK_FORTUNE, 0) + avariceLevel);
            enchantments.put(Enchantments.MOB_LOOTING, enchantments.getOrDefault(Enchantments.MOB_LOOTING, 0) + avariceLevel);
        }
    }

    @Override
    public int updateLooting(IToolStackView tool, ModifierEntry modifier, LootingContext context, int looting) {
        int avariceLevel = tool.getPersistentData().getInt(AVARICE_LEVEL);
        if (avariceLevel > 0) {
            tool.getPersistentData().remove(AVARICE_LEVEL);
            return looting + avariceLevel;
        }
        return looting;
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, @NotNull ItemStack held, Slot slot, @NotNull Player player, @NotNull SlotAccess access) {
        if (held.isEmpty()) return false;

        ModDataNBT data = tool.getPersistentData();
        long currentProgress = data.getInt(AVARICE_PROGRESS);
        long currentLevel = data.getInt(AVARICE_LEVEL);

        int count = held.getCount();
        int valuePerItem = (held.getMaxStackSize() > 16) ? 1 : 64;
        long totalAddedProgress = (long) count * valuePerItem;

        if (!player.getAbilities().instabuild) {
            held.setCount(0);
        }

        currentProgress += totalAddedProgress;
        if (currentProgress >= 64) {
            currentLevel += (currentProgress / 64);
            currentProgress %= 64;
        }

        data.putInt(AVARICE_LEVEL, (int)Math.min(currentLevel, 1000000));
        data.putInt(AVARICE_PROGRESS, (int)currentProgress);

        player.displayClientMessage(Component.translatable("message.miztinker.pot_of_avarice.progress", currentProgress)
                .append(" (Level: " + data.getInt(AVARICE_LEVEL) + ")"), true);

        return true;
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (tool.getPersistentData().getInt(AVARICE_LEVEL) > 0) {
            tool.getPersistentData().remove(AVARICE_LEVEL);
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        int level = data.getInt(AVARICE_LEVEL);
        int progress = data.getInt(AVARICE_PROGRESS);

        if (level > 0) {
            tooltip.add(Component.translatable("tooltip.miztinker.pot_of_avarice.level", level).withStyle(ChatFormatting.GOLD));
            if (progress > 0) {
                tooltip.add(Component.translatable("message.miztinker.pot_of_avarice.progress", progress).withStyle(ChatFormatting.GRAY));
            }
        } else if (progress > 0) {
            tooltip.add(Component.translatable("message.miztinker.pot_of_avarice.progress", progress).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.miztinker.pot_of_avarice.empty").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}