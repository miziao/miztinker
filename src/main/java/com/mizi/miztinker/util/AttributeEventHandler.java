package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.modifiers.SuperLollipop;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class AttributeEventHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        SuperLollipop.applyPermanentAttributes(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerLoggedInEvent event) {
        SuperLollipop.applyPermanentAttributes(event.getEntity());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        SuperLollipop.applyPermanentAttributes(event.getEntity());
    }
}