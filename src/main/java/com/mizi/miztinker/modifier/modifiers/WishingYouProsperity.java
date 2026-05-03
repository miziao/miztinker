package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook; // 新增
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class WishingYouProsperity extends Modifier implements
        MeleeHitModifierHook, BlockBreakModifierHook, BlockInteractionModifierHook, OnAttackedModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    private void tryDropRedEnvelope(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && level.random.nextFloat() < 0.10f) {
            ItemStack redEnvelope = new ItemStack(MiztinkerItems.REDENEVLOPEITEM.get());
            ItemEntity entity = new ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    redEnvelope);
            entity.setPickUpDelay(10);
            level.addFreshEntity(entity);

            player.displayClientMessage(Component.translatable("miztinker.modifier.wishing_you_prosperity.drop"), true);
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (context.getAttacker() instanceof Player player) {
            tryDropRedEnvelope(context.getLevel(), context.getTarget().blockPosition(), player);
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.getLiving() instanceof Player player) {
            tryDropRedEnvelope(context.getWorld(), context.getPos(), player);
        }
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        if (source == InteractionSource.RIGHT_CLICK && context.getPlayer() != null) {
            tryDropRedEnvelope(context.getLevel(), context.getClickedPos(), context.getPlayer());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (context.getEntity() instanceof Player player && !player.level().isClientSide) {
            tryDropRedEnvelope(player.level(), player.blockPosition(), player);
        }
    }
}
