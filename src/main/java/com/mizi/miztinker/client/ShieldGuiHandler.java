package com.mizi.miztinker.client;

import com.mizi.miztinker.modifier.modifiers.EquivalentArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "miztinker")
public class ShieldGuiHandler {
    private static float lerpShield = -1;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int totalLevel = EquivalentArmor.getTotalLevel(mc.player);
        if (totalLevel <= 0) return;

        CompoundTag data = mc.player.getPersistentData().getCompound(EquivalentArmor.SHIELD_NBT);
        float currentShield = data.getFloat(EquivalentArmor.SHIELD_VAL);
        float maxShield = totalLevel * 2000f;
        int cooldown = data.getInt(EquivalentArmor.SHIELD_COOLDOWN);

        if (lerpShield < 0) lerpShield = currentShield;
        if (cooldown > 0) {
            lerpShield = 0;
        } else {
            lerpShield = Mth.lerp(0.15f, lerpShield, currentShield);
            if (Math.abs(lerpShield - currentShield) < 0.5f) lerpShield = currentShield;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (cooldown > 0) {
            float pulse = 0;
            if (mc.level != null) {
                pulse = (float) Math.abs(Math.sin(mc.level.getGameTime() * 0.2));
            }
            int alpha = (int) (25 + (pulse * 40));
            int pulseColor = (alpha << 24) | 0x660000;
            graphics.fill(0, 0, screenWidth, 2, pulseColor);
            graphics.fill(0, screenHeight - 2, screenWidth, screenHeight, pulseColor);
        }

        int x = (screenWidth / 2) + 130;
        int y = screenHeight - 55;
        int width = 130;
        int height = 48;

        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0xFF000000);
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF333333);
        graphics.fill(x, y, x + width, y + height, 0xDD111111);

        int accentColor = cooldown > 0 ? 0xFF990000 : 0xFF00E6E6;
        graphics.fill(x - 4, y, x - 1, y + height, 0xFF000000);
        graphics.fill(x - 3, y + 1, x - 2, y + height - 1, accentColor);

        graphics.drawString(mc.font, "§c" + Component.translatable("gui.miztinker.emc_shield.title").getString(), x + 5, y + 5, 0xFFFFFF);

        String statusKey = cooldown > 0 ? "gui.miztinker.emc_shield.recharging" : "gui.miztinker.emc_shield.stable";
        int statusColor = cooldown > 0 ? 0xFF5555 : 0x00FFFF;
        graphics.drawString(mc.font, "§l» §r" + Component.translatable(statusKey).getString(), x + 8, y + 17, statusColor, false);

        int barX = x + 8;
        int barY = y + 30;
        int barW = width - 20;
        int barH = 5;

        graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF000000);
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF222222);

        float pct = maxShield > 0 ? Mth.clamp(lerpShield / maxShield, 0, 1) : 0;
        int currentBarW = (int) (barW * pct);
        if (currentBarW > 0) {
            graphics.fill(barX, barY, barX + currentBarW, barY + barH, accentColor);
            int glowAlpha = 0;
            if (mc.level != null) {
                glowAlpha = (int) (80 + 40 * Math.sin(mc.level.getGameTime() * 0.1));
            }
            graphics.fill(barX, barY, barX + currentBarW, barY + 1, (glowAlpha << 24) | 0xFFFFFF);
        }

        String formattedValue = Component.translatable("gui.miztinker.emc_shield.value",
                (int)Math.ceil(lerpShield),
                (int)maxShield).getString();

        graphics.drawString(mc.font, "§7" + formattedValue, barX, y + 38, 0xCCCCCC, false);
    }
}