package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.isFromDummmmmmyMod;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.reflectionPenetratingDamage;
import static net.minecraft.world.entity.ai.attributes.Attributes.ARMOR;

public class ColorModifier extends NoLevelsModifier implements MeleeHitModifierHook, TooltipModifierHook, MeleeDamageModifierHook {

    private static final String MODID = "miztinker";
    private static final ResourceLocation ENTITY_COLOR_RED = ResourceLocation.fromNamespaceAndPath(MODID, "entity_color_red");
    private static final ResourceLocation ENTITY_COLOR_GREEN = ResourceLocation.fromNamespaceAndPath(MODID, "entity_color_green");
    private static final ResourceLocation ENTITY_COLOR_BLUE = ResourceLocation.fromNamespaceAndPath(MODID, "entity_color_blue");

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity target = context.getLivingTarget();
        if (target != null) {
            ModDataNBT data = tool.getPersistentData();
            int[] rgb = getEntityColor(target);

            data.putInt(ENTITY_COLOR_RED, rgb[0]);
            data.putInt(ENTITY_COLOR_GREEN, rgb[1]);
            data.putInt(ENTITY_COLOR_BLUE, rgb[2]);
        }
        return damage;
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target != null) {
            ModDataNBT data = tool.getPersistentData();
            var holder = context.getAttacker();

            float greenValue = data.getFloat(ENTITY_COLOR_GREEN);
            holder.heal(greenValue);
            if (target.getAttribute(ARMOR) == null) return;
            float blueValue = data.getFloat(ENTITY_COLOR_BLUE);
            float armorValue = target.getArmorValue();
            Objects.requireNonNull(target.getAttribute(ARMOR)).setBaseValue(armorValue - blueValue);
            if (isFromDummmmmmyMod(target)) return;

            float redValue = data.getFloat(ENTITY_COLOR_RED);
            reflectionPenetratingDamage(target,holder,redValue);
        }

    }
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        int red = data.getInt(ENTITY_COLOR_RED);
        int green = data.getInt(ENTITY_COLOR_GREEN);
        int blue = data.getInt(ENTITY_COLOR_BLUE);

        long time = player != null ? player.level().getGameTime() : System.currentTimeMillis() / 50;
        float hue = (time % 120) / 120.0f;
        int rainbow = java.awt.Color.getHSBColor(hue, 0.7f, 0.9f).getRGB() & 0xFFFFFF;

        if (!tooltip.isEmpty()) {
            tooltip.set(0, tooltip.get(0).copy().withStyle(s -> s.withColor(rainbow)));
        }

        Component nameComp = Component.translatable(getTranslationKey());
        String nameStripped = nameComp.getString();
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getString().contains(nameStripped)) {
                tooltip.set(i, nameComp.copy().withStyle(s -> s.withColor(rainbow)));
            }
        }

        if (tooltipKey == TooltipKey.SHIFT) {
            MutableComponent rComp = Component.literal(String.valueOf(red)).withStyle(style -> style.withColor(ChatFormatting.RED));
            MutableComponent gComp = Component.literal(String.valueOf(green)).withStyle(style -> style.withColor(ChatFormatting.GREEN));
            MutableComponent bComp = Component.literal(String.valueOf(blue)).withStyle(ChatFormatting.BLUE);

            MutableComponent separator = Component.literal(", ").withStyle(ChatFormatting.GRAY);

            MutableComponent rgbDisplay = Component.translatable("tooltip.color_enchant.rgb_values")
                    .withStyle(style -> style.withColor(rainbow))
                    .append(rComp).append(separator)
                    .append(gComp).append(separator)
                    .append(bComp);

            tooltip.add(rgbDisplay);
        }
    }

    private int getRainbowColor(long tick, float speed) {
        float hue = ((tick * speed) % 120) / 120.0f;
        java.awt.Color color = java.awt.Color.getHSBColor(hue, 0.7f, 0.9f);
        return color.getRGB() & 0xFFFFFF;
    }
    public static int[] getEntityColor(LivingEntity entity) {
        if (entity == null || !entity.level().isClientSide) {
            return new int[]{0, 0, 0};
        }

        if (entity instanceof EnderDragon) return new int[]{0, 0, 0};

        try {
            EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            if (renderer == null) return new int[]{0, 0, 0};

            ResourceLocation texture = renderer.getTextureLocation(entity);

            try (InputStream stream = Minecraft.getInstance().getResourceManager()
                    .getResource(texture).orElseThrow().open()) {
                BufferedImage image = ImageIO.read(stream);
                if (image != null) {
                    return calculateAverageColor(image);
                }
            }
        } catch (Exception e) {
        }
        return new int[]{0, 0, 0};
    }

    private static int[] calculateAverageColor(BufferedImage image) {
        final int sampleStep = 8; // 采样步长
        long r = 0, g = 0, b = 0;
        int count = 0;

        for (int y = 0; y < image.getHeight(); y += sampleStep) {
            for (int x = 0; x < image.getWidth(); x += sampleStep) {
                int argb = image.getRGB(x, y);
                if (((argb >> 24) & 0xFF) > 128) { // 忽略透明像素
                    r += (argb >> 16) & 0xFF;
                    g += (argb >> 8) & 0xFF;
                    b += argb & 0xFF;
                    count++;
                }
            }
        }

        if (count == 0) return new int[]{255, 255, 255};
        return new int[]{(int)(r / count), (int)(g / count), (int)(b / count)};
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }
}