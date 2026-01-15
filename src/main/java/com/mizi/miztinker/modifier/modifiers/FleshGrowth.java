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

    private static final UUID REACH_UUID = UUID.fromString("b9c2a13e-3231-47cd-a150-e5b1eb9c7172");
    private static final UUID ATTACK_UUID = UUID.fromString("d2a13eb9-3231-47cd-b150-e5b1eb9c7173");

    private ResourceLocation getGrowthKey() {
        return new ResourceLocation("miztinker", TAG_GROWTH);
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
        ResourceLocation key = getGrowthKey();

        float currentGrowth = data.getFloat(key);
        data.putFloat(key, currentGrowth + GAIN_PER_KILL);
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier,
                              EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            ModDataNBT data = tool.getPersistentData();
            float growth = data.getFloat(getGrowthKey());

            if (growth > 0) {

                consumer.accept(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(
                        REACH_UUID,
                        "Flesh Growth Reach",
                        growth,
                        AttributeModifier.Operation.ADDITION
                ));

                consumer.accept(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(
                        ATTACK_UUID,
                        "Flesh Growth Attack",
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

        tooltip.add(Component.literal("血肉生长")
                .withStyle(ChatFormatting.RED));

        tooltip.add(Component.literal(
                        String.format("额外延伸: +%.1f", growth))
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal("这株藤蔓已失去控制，它正通过掠夺生命无限延伸...")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }
}