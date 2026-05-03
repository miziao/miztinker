package com.mizi.miztinker.entity.boss.bartest;


import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MiziAoBartest {
    static Minecraft mc = Minecraft.getInstance();
    static Window window = mc.getWindow();

    private static final Component NORMAL_NAME = Component.translatable("entity.miztinker.mizi_ao");
    private static final Component CHUJING_NAME = Component.translatable("entity.miztinker.mizi_ao.chujing");


    public static int rgba(int red, int green, int blue, int alpha) {
        alpha %= 256;
        red %= 256;
        green %= 256;
        blue %= 256;
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * 用白色和青色交替的8段条纹来填充一个区域。
     *
     * @param guiGraphics   GuiGraphics实例
     * @param x             起始x坐标
     * @param y             起始y坐标
     * @param width         要填充的总宽度
     * @param height        填充的高度
     */
    public static void fillStripedBar(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // 1. 定义我们需要的两种颜色
        int whiteColor = Color.WHITE.getRGB(); // 白色
        int cyanColor = Color.CYAN.getRGB();   // 青色

        // 2. 计算出每一段的精确宽度
        float segmentWidth = (float) width / 8.0f;

        // 如果总宽度太小，无法分成8段，则直接用一种颜色填充以避免除零错误
        if (segmentWidth <= 0) {
            guiGraphics.fill(x, y, x + width, y + height, whiteColor | 0xFF000000);
            return;
        }

        // 3. 遍历需要绘制的每一个像素
        for (int i = 0; i < width; i++) {
            // 4. 计算当前像素属于第几段 (0 到 7)
            int segmentIndex = (int) (i / segmentWidth);

            // 5. 根据段的索引是奇数还是偶数来选择颜色
            int currentColor;
            if (segmentIndex % 2 == 0) {
                // 偶数段 (0, 2, 4, 6) 使用白色
                currentColor = whiteColor;
            } else {
                // 奇数段 (1, 3, 5, 7) 使用青色
                currentColor = cyanColor;
            }

            // 6. 绘制当前像素
            // 加上 0xFF000000 是为了确保Alpha通道是不透明的
            guiGraphics.fill(x + i, y, x + i + 1, y + height, currentColor | 0xFF000000);
        }
    }

    public static void drawIrregularBorder(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int borderColor = rgba(180, 180, 180, 200);
        int shadowColor = rgba(50, 50, 50, 150);

        // 主边框（1像素宽）
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);         // 上边框
        guiGraphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor); // 下边框
        guiGraphics.fill(x - 1, y, x, y + height, borderColor);                // 左边框
        guiGraphics.fill(x + width, y, x + width + 1, y + height, borderColor);// 右边框

        // 阴影效果（外扩1像素）
        guiGraphics.fill(x - 2, y - 2, x + width + 2, y - 1, shadowColor);    // 顶部阴影
        guiGraphics.fill(x - 2, y + height + 1, x + width + 2, y + height + 2, shadowColor); // 底部阴影
        guiGraphics.fill(x - 2, y - 1, x - 1, y + height + 1, shadowColor);   // 左侧阴影
        guiGraphics.fill(x + width + 1, y - 1, x + width + 2, y + height + 1, shadowColor); // 右侧阴影

        int spikeCount = 5;
        int spikeWidth = 2;
        for (int i = 0; i < spikeCount; i++) {
            float pos = (float)i / (spikeCount - 1);
            int spikeX = x + (int)(width * pos);
            int spikeHeight = 4 + (int)(Math.sin(pos * Math.PI * 2  * 0.02) * 3); // 高度增加

            // 左侧尖刺（加宽版）
            guiGraphics.fill(spikeX - spikeWidth, y - spikeHeight, spikeX, y, borderColor);

            // 右侧尖刺（加宽版）
            guiGraphics.fill(spikeX - spikeWidth, y + height, spikeX, y + height + spikeHeight, borderColor);

        }

        int xThickness = 2; // 斜线粗细
        int xLength = 10;   // 斜线长度

        // 左上到右下斜线
        drawDiagonalLine(guiGraphics,
                x, y - 1,
                x + xLength, y - 1 - xLength/2,
                xThickness, borderColor);

        // 右上到左下斜线
        drawDiagonalLine(guiGraphics,
                x + width, y - 1,
                x + width - xLength, y - 1 - xLength/2,
                xThickness, borderColor);

        // 左下到右上斜线
        drawDiagonalLine(guiGraphics,
                x, y + height + 1,
                x + xLength, y + height + 1 + xLength/2,
                xThickness, borderColor);

        // 右下到左上斜线
        drawDiagonalLine(guiGraphics,
                x + width, y + height + 1,
                x + width - xLength, y + height + 1 + xLength/2,
                xThickness, borderColor);
    }

    // 优化的斜线绘制方法
    private static void drawDiagonalLine(GuiGraphics guiGraphics,
                                         int x1, int y1, int x2, int y2, int thickness, int color) {

        // 确保从左到右绘制
        if (x1 > x2) {
            int tx = x1; x1 = x2; x2 = tx;
            int ty = y1; y1 = y2; y2 = ty;
        }

        float slope = (float)(y2 - y1) / (x2 - x1);
        for (int x = x1; x <= x2; x++) {
            int y = y1 + (int)(slope * (x - x1));
            // 绘制有厚度的线
            for (int t = 0; t < thickness; t++) {
                guiGraphics.fill(x, y + t, x + 1, y + t + 1, color);
            }
        }
    }

    @SubscribeEvent
    public static void bartest(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent bossStatusInfo = event.getBossEvent();

        String nameStr = bossStatusInfo.getName().getString();
        String targetName = Component.translatable("entity.jzyy.mizi_ao").getString();
        String targetNameChujing = Component.translatable("entity.jzyy.mizi_ao.chujing").getString();

        if (nameStr.startsWith(targetName) || nameStr.startsWith(targetNameChujing)) {

            event.setCanceled(true);

            if (mc.level != null) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                GuiGraphics guiGraphics = event.getGuiGraphics();

                int width = 256;
                int height = 8;
                int x = event.getWindow().getGuiScaledWidth() / 2 - width / 2;
                int y = event.getY();

                drawIrregularBorder(guiGraphics, x, y, width, height);

                guiGraphics.fill(x, y, x + width, y + height, 0xB41E1E1E);

                float healthPercentage = bossStatusInfo.getProgress();
                int progressWidth = (int) (width * healthPercentage);

                if (progressWidth > 0) {
                    fillStripedBar(guiGraphics, x, y, progressWidth, height);
                }
                Font font = mc.font;
                int nameWidth = font.width(nameStr);
                int nameX = x + (width / 2) - (nameWidth / 2);
                int nameY = y - 12;

                guiGraphics.drawString(font, nameStr, nameX, nameY, 0xFF00FFFF, true);

                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
}
