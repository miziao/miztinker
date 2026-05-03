package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;

import static com.mizi.miztinker.miztinker.getResource;

public class SRPHeal extends Modifier implements InventoryTickModifierHook, OnAttackedModifierHook, TooltipModifierHook {

    public static final ResourceLocation EXTRA_HEAL = getResource("srp_heal_extra");
    private static final String KEY_TOOLTIP = "modifier.miztinker.srp_heal.strength";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (!context.getEntity().level().isClientSide && !context.getEntity().isOnFire()) {
            ModDataNBT data = tool.getPersistentData();
            int currentExtra = data.getInt(EXTRA_HEAL);
            data.putInt(EXTRA_HEAL, currentExtra + 1);
        }
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && isCorrectSlot) {
            ModDataNBT data = tool.getPersistentData();

            if (holder.isOnFire()) {
                if (data.getInt(EXTRA_HEAL) > 0) {
                    data.putInt(EXTRA_HEAL, 0);
                }
                return;
            }

            if (holder.tickCount % 20 == 0 && holder.getHealth() < holder.getMaxHealth()) {
                float baseHeal = (float) entry.getLevel();
                float extraHeal = (float) data.getInt(EXTRA_HEAL);

                holder.heal(baseHeal + extraHeal);
            }
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltips, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        int extraHeal = tool.getPersistentData().getInt(EXTRA_HEAL);
        int percent = extraHeal * 100;

        tooltips.add(Component.translatable(KEY_TOOLTIP, percent));
    }
}