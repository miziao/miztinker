package com.mizi.miztinker.client;

import com.mizi.miztinker.modifier.modifiers.base.GhostfreakHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class GhostfreakLoginSync {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // ⚡ 登录时强制重置为“解除变身”
        GhostfreakHelper.setGhostActive(player, false);

        // 禁止飞行
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;

        // 更新能力
        player.onUpdateAbilities();
    }
}