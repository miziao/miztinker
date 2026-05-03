package com.mizi.miztinker.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MizShaderClient {
    private static final ResourceLocation INVERT_SHADER = ResourceLocation.fromNamespaceAndPath("minecraft", "post/invert.json");
    private static boolean isShaderActive = false;

    public static void setShaderActive(boolean active) {
        if (isShaderActive == active) return;
        isShaderActive = active;

        Minecraft mc = Minecraft.getInstance();
        mc.tell(() -> {
            if (active) {
                mc.gameRenderer.loadEffect(INVERT_SHADER);
            } else {
                mc.gameRenderer.shutdownEffect();
            }
        });
    }
}
