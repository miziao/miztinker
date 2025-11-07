package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.damagesource.DamageSource;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolActionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.modifiers.ability.interaction.BlockingModifier;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import static slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook.KEY_DRAWTIME;

public class BlockingDamage extends NoLevelsModifier
        implements GeneralInteractionModifierHook, ToolActionModifierHook,
        slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this,
                ModifierHooks.GENERAL_INTERACT,
                ModifierHooks.TOOL_ACTION,
                ModifierHooks.MODIFY_HURT
        );
    }

    /** 右键使用触发格挡动画 */
    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (source == InteractionSource.RIGHT_CLICK && !tool.isBroken()) {
            // 给 KEY_DRAWTIME 一个值，仅用来触发 Tinkers 的格挡动画
            tool.getPersistentData().putInt(KEY_DRAWTIME, 2);

            GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** 使用动作显示格挡 */
    @Override
    public UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) {
        return BlockingModifier.blockWhileCharging(tool, UseAnim.BLOCK);
    }

    /** 使用持续时间 */
    @Override
    public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
        return 72000;
    }

    /** 能做格挡动作 */
    @Override
    public boolean canPerformAction(IToolStackView tool, ModifierEntry entry, ToolAction toolAction) {
        return toolAction == ToolActions.SHIELD_BLOCK;
    }

    /** 格挡伤害减免 */
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry entry,
                                   EquipmentContext context, EquipmentSlot slotType,
                                   DamageSource source, float amount, boolean isDirectDamage) {
        LivingEntity entity = context.getEntity();
        if (entity != null && entity.isUsingItem() &&
                ModifierUtil.canPerformAction(tool, ToolActions.SHIELD_BLOCK)) {
            return amount * 0.5f;
        }
        return amount;
    }
}