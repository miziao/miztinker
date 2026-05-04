package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.client.OniMikoBowRender;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mizi.miztinker.miztinker;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;

@Mod.EventBusSubscriber(modid = miztinker.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    public static void init(IEventBus bus) {
        bus.addListener(ClientEventHandler::onAddLayers);
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> steve = event.getSkin("default");
        LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> alex = event.getSkin("slim");

        if (steve != null) steve.addLayer(new OniMikoBowRender(steve));
        if (alex != null) alex.addLayer(new OniMikoBowRender(alex));
    }
}