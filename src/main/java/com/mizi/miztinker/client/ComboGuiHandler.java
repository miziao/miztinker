package com.mizi.miztinker.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "miztinker")
public class ComboGuiHandler {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.level == null) return;

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return;

        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains("tic_persistent")) return;
        CompoundTag ticData = nbt.getCompound("tic_persistent");

        if (!ticData.contains("miztinker:combo_start_time")) return;
        long startTime = ticData.getLong("miztinker:combo_start_time");

        long currentTime = mc.level.getGameTime();
        long elapsedTicks = currentTime - startTime;

        if (elapsedTicks < 0 || elapsedTicks > 1200) return;

        int seconds = (int) (elapsedTicks / 20);
        seconds = Mth.clamp(seconds, 0, 60);

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int x = (screenWidth / 2) - 130 - 32;
        int y = screenHeight - 50;

        String frame = String.format("%02d", seconds);
        ResourceLocation clockFrameLocation = ResourceLocation.withDefaultNamespace("item/clock_" + frame);
        TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(clockFrameLocation);

        if (sprite != null) {
            graphics.blit(x, y, 0, 32, 32, sprite);
            if (seconds >= 50 && (currentTime % 10 < 5)) {
                graphics.fill(x, y, x + 32, y + 32, 0x33FF0000);
            }
        }
    }
}