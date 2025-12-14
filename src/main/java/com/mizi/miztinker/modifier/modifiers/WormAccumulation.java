package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class WormAccumulation extends Modifier
        implements MeleeHitModifierHook,
        AttributesModifierHook, TooltipModifierHook {

    /* ---------- 常量 ---------- */
    private static final String TAG_ARMOR = "worm_armor";
    private static final float ABSORB_RATIO = 0.01f; // 固定 1%
    private static final float ARMOR_PER_LEVEL = 100f;
    private static final UUID ARMOR_UUID = UUID.fromString("7a4a8b0f-2bfa-4a66-8d99-cc1a5d22c777");

    // ResourceLocation 常量，避免重复创建
    private ResourceLocation getArmorKey() {
        return new ResourceLocation(getId().getNamespace(), getId().getPath() + "." + TAG_ARMOR);
    }

    /* ---------- Hook 注册 ---------- */
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this,
                ModifierHooks.MELEE_HIT,
                ModifierHooks.ATTRIBUTES,
                ModifierHooks.TOOLTIP);
    }

    /* ---------- 击杀吸收 ---------- */
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier,
                              ToolAttackContext context, float damageDealt) {

        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();

        if (player == null || target == null || target.isAlive()) return;
        if (player.level().isClientSide) return;

        ModDataNBT data = tool.getPersistentData();
        ResourceLocation key = getArmorKey();

        float current = data.getFloat(key);
        float gain = target.getMaxHealth() * ABSORB_RATIO;
        float max = modifier.getLevel() * ARMOR_PER_LEVEL;
        float newValue = Math.min(current + gain, max);

        data.putFloat(key, newValue);
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier,
                              EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
        // Only apply when held in main hand or off hand
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            ModDataNBT data = tool.getPersistentData();
            ResourceLocation key = getArmorKey();
            float storedArmor = data.getFloat(key);

            if (storedArmor > 0) {
                consumer.accept(Attributes.ARMOR, new AttributeModifier(
                        ARMOR_UUID,
                        "worm_accumulation_armor",
                        storedArmor,
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }


    /* ---------- 工具提示 ---------- */
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier,
                           Player player, List<Component> tooltip,
                           TooltipKey key, TooltipFlag flag) {

        ModDataNBT data = tool.getPersistentData();
        ResourceLocation nbtKey = getArmorKey();
        float armor = data.getFloat(nbtKey);
        float max = modifier.getLevel() * ARMOR_PER_LEVEL;

        tooltip.add(Component.literal("蠕虫堆积")
                .withStyle(ChatFormatting.DARK_GREEN));

        tooltip.add(Component.literal(
                        String.format("蠕虫厚度: %.1f / %.0f", armor, max))
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.literal(
                        "击杀生物吸收其 1% 生命值堆积为蠕虫厚度")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}