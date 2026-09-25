package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;
import java.util.Objects;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.isFromDummmmmmyMod;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.reflectionPenetratingDamage;
import static net.minecraft.world.entity.ai.attributes.Attributes.ARMOR;

@SuppressWarnings({"deprecation", "removal"})
public class ColorModifier extends NoLevelsModifier implements MeleeHitModifierHook, TooltipModifierHook {

    private static final String MODID = "miztinker";
    public static final ModifierId ID = new ModifierId(MODID, "color");
    public static final ResourceLocation ENTITY_COLOR_RED = new ResourceLocation(MODID, "entity_color_red");
    public static final ResourceLocation ENTITY_COLOR_GREEN = new ResourceLocation(MODID, "entity_color_green");
    public static final ResourceLocation ENTITY_COLOR_BLUE = new ResourceLocation(MODID, "entity_color_blue");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity holder = context.getAttacker();

        if (target == null || holder == null || holder instanceof FakePlayer) return;

        if (!holder.level.isClientSide) {
            ModDataNBT data = tool.getPersistentData();

            int greenValue = data.getInt(ENTITY_COLOR_GREEN);
            if (greenValue > 0) {
                holder.heal(greenValue / 255f);
            }

            int blueValue = data.getInt(ENTITY_COLOR_BLUE);
            if (target.getAttribute(ARMOR) != null && blueValue > 0) {
                float currentArmor = (float) target.getAttributeValue(ARMOR);
                Objects.requireNonNull(target.getAttribute(ARMOR)).setBaseValue(Math.max(0, currentArmor - (blueValue / 100f)));
            }

            if (!isFromDummmmmmyMod(target)) {
                int redValue = data.getInt(ENTITY_COLOR_RED);
                if (redValue > 0) {
                    reflectionPenetratingDamage(target, holder, redValue / 50f);
                }
            }
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        int red = data.getInt(ENTITY_COLOR_RED);
        int green = data.getInt(ENTITY_COLOR_GREEN);
        int blue = data.getInt(ENTITY_COLOR_BLUE);

        long time = player != null ? player.level.getGameTime() : System.currentTimeMillis() / 50;
        float hue = (time % 120) / 120.0f;
        int rainbow = java.awt.Color.getHSBColor(hue, 0.7f, 0.9f).getRGB() & 0xFFFFFF;

        if (!tooltip.isEmpty()) {
            tooltip.set(0, tooltip.get(0).copy().withStyle(s -> s.withColor(rainbow)));
        }

        if (tooltipKey == TooltipKey.SHIFT) {
            MutableComponent rComp = Component.literal(String.valueOf(red)).withStyle(s -> s.withColor(0xFF5555));
            MutableComponent gComp = Component.literal(String.valueOf(green)).withStyle(s -> s.withColor(0x55FF55));
            MutableComponent bComp = Component.literal(String.valueOf(blue)).withStyle(s -> s.withColor(0x5555FF));
            MutableComponent separator = Component.literal(", ").withStyle(ChatFormatting.GRAY);

            tooltip.add(Component.translatable("tooltip.miztinker.rgb_values")
                    .withStyle(s -> s.withColor(rainbow))
                    .append(rComp).append(separator)
                    .append(gComp).append(separator)
                    .append(bComp));
        }
    }
}