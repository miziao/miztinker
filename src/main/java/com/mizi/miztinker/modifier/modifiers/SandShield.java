package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.RepairFactorModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

import static com.mizi.miztinker.miztinker.getResource;

public class SandShield extends NoLevelsModifier implements OnAttackedModifierHook, ToolStatsModifierHook, RepairFactorModifierHook, TooltipModifierHook {

    private static final ResourceLocation ARMOR_LOSS = getResource("sand_shield_loss");
    private static final float LOSS_PER_HIT = 2.0f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
        hookBuilder.addHook(this, ModifierHooks.REPAIR_FACTOR);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (slotType.getType() == EquipmentSlot.Type.ARMOR && !context.getEntity().level().isClientSide) {
            ModDataNBT data = tool.getPersistentData();
            float currentLoss = data.getFloat(ARMOR_LOSS);

            data.putFloat(ARMOR_LOSS, currentLoss + LOSS_PER_HIT);

            if (tool instanceof ToolStack toolStack) {
                toolStack.rebuildStats();
            }
        }
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        float loss = context.getPersistentData().getFloat(ARMOR_LOSS);
        if (loss > 0) {
            ToolStats.ARMOR.add(builder, -loss);
            ToolStats.ARMOR_TOUGHNESS.add(builder, -loss);
        }
    }

    @Override
    public float getRepairFactor(IToolStackView tool, ModifierEntry entry, float factor) {
        ModDataNBT data = tool.getPersistentData();
        if (data.getFloat(ARMOR_LOSS) > 0) {
            data.putFloat(ARMOR_LOSS, 0);
        }
        return factor;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        float loss = tool.getPersistentData().getFloat(ARMOR_LOSS);
        if (loss > 0) {
            tooltip.add(Component.translatable("modifier.miztinker.sand_shield.debuff", String.format("%.1f", loss)));
        } else {
            tooltip.add(Component.translatable("modifier.miztinker.sand_shield.active"));
        }
    }
}