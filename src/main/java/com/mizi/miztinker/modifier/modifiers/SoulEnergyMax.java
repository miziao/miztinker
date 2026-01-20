package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class SoulEnergyMax extends NoLevelsModifier implements GeneralInteractionModifierHook, MeleeHitModifierHook, ToolStatsModifierHook {

    private static final ResourceLocation DAMAGE_KEY = new ResourceLocation("miztinker", "sacrifice_damage");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide) return InteractionResult.PASS;
        if (source != InteractionSource.RIGHT_CLICK || !player.isCrouching() || tool.isBroken()) return InteractionResult.PASS;

        int currentSlot = player.getInventory().selected;
        int leftSlot = (currentSlot == 0) ? 8 : currentSlot - 1;
        int rightSlot = (currentSlot == 8) ? 0 : currentSlot + 1;

        ItemStack leftStack = player.getInventory().getItem(leftSlot);
        ItemStack rightStack = player.getInventory().getItem(rightSlot);

        if (isTinkerTool(leftStack) && isTinkerTool(rightStack)) {
            leftStack.setCount(0);
            rightStack.setCount(0);

            tool.getPersistentData().putFloat(DAMAGE_KEY, Float.POSITIVE_INFINITY);

            ToolStack.from(player.getItemInHand(hand)).rebuildStats();

            player.displayClientMessage(Component.literal("§9这就是欧贝利斯克的巨神兵的最上级能力,灵魂充能MAX!"), true);
            return InteractionResult.SUCCESS;
        } else {
            player.displayClientMessage(Component.literal("§c只有匠魂工具才可以被献祭!"), true);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        Level level = context.getAttacker().level();
        if (level.isClientSide) return;

        ModDataNBT data = tool.getPersistentData();
        if (data.contains(DAMAGE_KEY) && Float.isInfinite(data.getFloat(DAMAGE_KEY))) {
            data.remove(DAMAGE_KEY);

            if (context.getPlayerAttacker() != null) {
                ToolStack.from(context.getPlayerAttacker().getMainHandItem()).rebuildStats();
            }
        }
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        ModDataNBT data = (ModDataNBT) context.getPersistentData();
        if (data.contains(DAMAGE_KEY)) {
            float value = data.getFloat(DAMAGE_KEY);
            if (Float.isInfinite(value)) {
                ToolStats.ATTACK_DAMAGE.update(builder, Float.POSITIVE_INFINITY);
            }
        }
    }

    private boolean isTinkerTool(ItemStack stack) {
        return !stack.isEmpty() && stack.is(TinkerTags.Items.MODIFIABLE);
    }
}