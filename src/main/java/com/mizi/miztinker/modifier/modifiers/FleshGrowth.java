package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.ForgeMod;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class FleshGrowth extends NoLevelsModifier
        implements MeleeHitModifierHook, AttributesModifierHook, TooltipModifierHook {

    private static final String TAG_GROWTH = "flesh_growth_value";
    private static final float GAIN_PER_KILL = 0.5f;

    private static final UUID MAIN_HAND_REACH = UUID.fromString("b9c2a13e-3231-47cd-a150-e5b1eb9c7172");
    private static final UUID MAIN_HAND_ATTACK = UUID.fromString("d2a13eb9-3231-47cd-b150-e5b1eb9c7173");
    private static final UUID OFF_HAND_REACH = UUID.fromString("c1d3b24f-4342-58de-b261-f6c2fc0d8284");
    private static final UUID OFF_HAND_ATTACK = UUID.fromString("e3b24fc0-4342-58de-c261-f6c2fc0d8285");

    private static final String KEY_NAME = "modifier.miztinker.flesh_growth";
    private static final String KEY_STAT = "modifier.miztinker.flesh_growth.stat";
    private static final String KEY_FLAVOR = "modifier.miztinker.flesh_growth.flavor";

    private ResourceLocation getGrowthKey() {
        return ResourceLocation.fromNamespaceAndPath("miztinker", TAG_GROWTH);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this,
                ModifierHooks.MELEE_HIT,
                ModifierHooks.ATTRIBUTES,
                ModifierHooks.TOOLTIP);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier,
                              ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();

        if (player == null || target == null || target.isAlive()) return;
        if (player.level().isClientSide) return;

        ModDataNBT data = tool.getPersistentData();
        float currentGrowth = data.getFloat(getGrowthKey());
        data.putFloat(getGrowthKey(), currentGrowth + GAIN_PER_KILL);
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier,
                              EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            float growth = tool.getPersistentData().getFloat(getGrowthKey());

            if (growth > 0) {
                UUID reachUUID = (slot == EquipmentSlot.MAINHAND) ? MAIN_HAND_REACH : OFF_HAND_REACH;
                UUID attackUUID = (slot == EquipmentSlot.MAINHAND) ? MAIN_HAND_ATTACK : OFF_HAND_ATTACK;

                consumer.accept(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(
                        reachUUID,
                        "Flesh Growth Reach " + slot.getName(),
                        growth,
                        AttributeModifier.Operation.ADDITION
                ));

                consumer.accept(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(
                        attackUUID,
                        "Flesh Growth Attack " + slot.getName(),
                        growth,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier,
                           Player player, List<Component> tooltip,
                           TooltipKey key, TooltipFlag flag) {

        float growth = tool.getPersistentData().getFloat(getGrowthKey());

        tooltip.add(Component.translatable(KEY_NAME)
                .withStyle(ChatFormatting.RED));

        tooltip.add(Component.translatable(KEY_STAT, String.format("%.1f", growth))
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("modifier.miztinker.flesh_growth.description")
                .withStyle(ChatFormatting.GOLD));

    }
}