package com.mizi.miztinker.client;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.WeakHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class TigaShieldRenderTracker {

    private static final Map<Player, Boolean> SHIELD_MAP = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof Player player) {
            SHIELD_MAP.put(player, false);
        }
    }

    public static void setShieldActive(Player player, boolean active) {
        SHIELD_MAP.put(player, active);
    }

    public static boolean hasShield(Player player) {
        return SHIELD_MAP.getOrDefault(player, false);
    }
}