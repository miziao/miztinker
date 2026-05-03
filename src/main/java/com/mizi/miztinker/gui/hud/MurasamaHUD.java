package com.mizi.miztinker.gui.hud;


import com.mizi.miztinker.gui.hudhelder.MurasamaDrawTime;
import com.mizi.miztinker.miztinker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

import static com.mizi.miztinker.item.tool.until.MiztinkerTools.murasama;

public class MurasamaHUD {
    public static ResourceLocation energy_quantity0 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/1.png");
    public static ResourceLocation energy_quantity1 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/11.png");
    public static ResourceLocation energy_quantity2 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/12.png");
    public static ResourceLocation energy_quantity3 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/13.png");
    public static ResourceLocation energy_quantity4 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/14.png");
    public static ResourceLocation energy_quantity5 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/15.png");
    public static ResourceLocation energy_quantity6 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/16.png");
    public static ResourceLocation energy_quantity7 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/17.png");
    public static ResourceLocation energy_quantity8 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/18.png");
    public static ResourceLocation energy_quantity9 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/19.png");
    public static ResourceLocation energy_quantity10 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/110.png");
    public static List<ResourceLocation> energy_quantity = List.of(energy_quantity0,energy_quantity1,energy_quantity2,energy_quantity3,energy_quantity4,energy_quantity5,energy_quantity6,energy_quantity7,energy_quantity8,energy_quantity9,energy_quantity10);

    public static ResourceLocation energy_point0 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/2.png");
    public static ResourceLocation energy_point1 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/21.png");
    public static ResourceLocation energy_point2 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/22.png");
    public static ResourceLocation energy_point3 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/23.png");
    public static ResourceLocation energy_point4 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/24.png");
    public static ResourceLocation energy_point5 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/25.png");
    public static ResourceLocation energy_point6 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/26.png");
    public static ResourceLocation energy_point7 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/27.png");
    public static ResourceLocation energy_point8 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/28.png");
    public static ResourceLocation energy_point9 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/29.png");
    public static ResourceLocation energy_point10 = ResourceLocation.fromNamespaceAndPath(miztinker.MODID,"/textures/gui/overlay/murasama_time/210.png");
    public static List<ResourceLocation> energy_point = List.of(energy_point0,energy_point1,energy_point2,energy_point3,energy_point4,energy_point5,energy_point6,energy_point7,energy_point8,energy_point9,energy_point10);


    public static IGuiOverlay Murasama_Energy_Quantity_HUD = ((gui, poseStack, partialTick, width, height) -> {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        if (!(player.getItemBySlot(EquipmentSlot.MAINHAND).is(murasama.get()))) {
            return;
        }
        if (ToolStack.from(player.getMainHandItem()).isBroken()) {
            return;
        }

        float energy_quantity_perc = MurasamaDrawTime.getEnergyQuantityPercentage();

        int energy_quantity_amount = Mth.clamp((int) (energy_quantity_perc * 10), 0, 10);

        int x = width / 2;
        int y = height / 2;

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, energy_quantity.get(energy_quantity_amount));
        poseStack.blit( energy_quantity.get(energy_quantity_amount), x-5, y - 17, 0, 0, 32, 32, 32, 32);

    });

    public static IGuiOverlay Murasama_Energy_Point_HUD = ((gui, poseStack, partialTick, width, height) -> {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        if (!(player.getItemBySlot(EquipmentSlot.MAINHAND).is(murasama.get()))) {
            return;
        }
        if (ToolStack.from(player.getMainHandItem()).isBroken()) {
            return;
        }

        float energy_point_perc = MurasamaDrawTime.getEnergyPointPercentage();

        int energy_point_amount = Mth.clamp((int) (energy_point_perc * 10), 0, 10);

        int x = width / 2;
        int y = height / 2;

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, energy_point.get(energy_point_amount));
        poseStack.blit(energy_point.get(energy_point_amount), x-29, y - 17, 0, 0, 32, 32, 32, 32);

    });
}
