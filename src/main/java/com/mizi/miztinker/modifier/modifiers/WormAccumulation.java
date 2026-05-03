package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;
import java.util.UUID;

public class WormAccumulation extends Modifier
        implements MeleeHitModifierHook, TooltipModifierHook {

    private static final String TAG_ARMOR = "worm_armor";
    private static final float ABSORB_RATIO = 0.01f; // 固定 1%
    private static final float ARMOR_PER_LEVEL = 100f;
    private static final UUID WORM_ARMOR_UUID = UUID.fromString("7a4a8b0f-2bfa-4a66-8d99-cc1a5d22c777");

    private ResourceLocation getArmorKey() {
        return ResourceLocation.fromNamespaceAndPath(getId().getNamespace(), getId().getPath() + "." + TAG_ARMOR);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this,
                ModifierHooks.MELEE_HIT,
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
        ResourceLocation key = getArmorKey();

        float current = data.getFloat(key);
        float gain = target.getMaxHealth() * ABSORB_RATIO;
        float max = modifier.getLevel() * ARMOR_PER_LEVEL;
        float newValue = Math.min(current + gain, max);

        data.putFloat(key, newValue);

        updatePlayerArmor(player, newValue);
    }

    private void updatePlayerArmor(Player player, float amount) {
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        if (armorAttr.getModifier(WORM_ARMOR_UUID) != null) {
            armorAttr.removeModifier(WORM_ARMOR_UUID);
        }

        armorAttr.addPermanentModifier(new AttributeModifier(
                WORM_ARMOR_UUID,
                "worm_accumulation_permanent_armor",
                amount,
                AttributeModifier.Operation.ADDITION
        ));
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier,
                           Player player, List<Component> tooltip,
                           TooltipKey key, TooltipFlag flag) {

        ModDataNBT data = tool.getPersistentData();
        ResourceLocation nbtKey = getArmorKey();
        float armor = data.getFloat(nbtKey);
        float max = modifier.getLevel() * ARMOR_PER_LEVEL;

        tooltip.add(Component.translatable(getTranslationKey())
                .withStyle(ChatFormatting.DARK_GREEN));

        tooltip.add(Component.translatable(getTranslationKey() + ".thickness",
                        String.format("%.1f", armor),
                        String.format("%.0f", max))
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable(getTranslationKey() + ".description")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}