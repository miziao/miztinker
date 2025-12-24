package com.mizi.miztinker.modifier.modifiers.base;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.server.level.ServerLevel;

public class ServerTickHandler {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            DelayedTaskHandler.tick(level);
        }
    }
}
