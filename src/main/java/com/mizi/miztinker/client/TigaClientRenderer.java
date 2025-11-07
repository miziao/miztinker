package com.mizi.miztinker.client;


import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TigaClientRenderer {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // 获取玩家渲染器
        PlayerRenderer playerRenderer = event.getSkin("default"); // 或 "slim" 根据你的需求

        if (playerRenderer != null) {
            playerRenderer.addLayer(new TigaShieldLayer(playerRenderer));
        }

        // 如果你要加到 slim 皮肤也可以
        PlayerRenderer slimRenderer = event.getSkin("slim");
        if (slimRenderer != null) {
            slimRenderer.addLayer(new TigaShieldLayer(slimRenderer));
        }
    }
}