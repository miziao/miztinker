package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.modifiers.EquivalentArmor;
import com.mizi.miztinker.modifier.modifiers.SuperLollipop;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class AttributeEventHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        EquivalentArmor.copyShieldData(event.getOriginal(), event.getEntity());
        SuperLollipop.applyPermanentAttributes(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EquivalentArmor.syncShieldState(player);
        }
        SuperLollipop.applyPermanentAttributes(event.getEntity());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EquivalentArmor.syncShieldState(player);
        }
        SuperLollipop.applyPermanentAttributes(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EquivalentArmor.syncShieldState(player);
        }
    }
}
