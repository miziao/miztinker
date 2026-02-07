package com.mizi.miztinker.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class ClientPacketHandler {
    public static void handle(PlaySoundPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null) return;
        // 计算相对音量（基于距离衰减）
        float distance = (float) player.distanceToSqr(packet.origin());
        float attenuatedVolume = packet.volume() * (1.0f - Mth.clamp(distance / 256f, 0f, 0.8f));

        level.playSound(
            player,
            packet.origin().x,
            packet.origin().y,
            packet.origin().z,
            BuiltInRegistries.SOUND_EVENT.get(packet.soundEvent()),
            SoundSource.PLAYERS,
            attenuatedVolume,
            packet.pitch()
        );
    }
}
