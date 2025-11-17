package com.mizi.miztinker.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端 → 服务端
 * 用于切换天气：0=晴天, 1=雨天, 2=雷暴
 */
public class WeatherChangePacket {
    private final int weatherState;

    public WeatherChangePacket(int weatherState) {
        this.weatherState = weatherState;
    }

    public static void encode(WeatherChangePacket msg, net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeInt(msg.weatherState);
    }

    public static WeatherChangePacket decode(net.minecraft.network.FriendlyByteBuf buf) {
        return new WeatherChangePacket(buf.readInt());
    }

    public static void handle(WeatherChangePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.level() instanceof ServerLevel server)) return;

            // 根据传入状态修改天气
            switch (msg.weatherState) {
                case 0 -> server.setWeatherParameters(12000, 0, false, false); // 晴天
                case 1 -> server.setWeatherParameters(0, 12000, true, false);  // 雨天
                case 2 -> server.setWeatherParameters(0, 12000, true, true);   // 雷暴
            }
        });
        ctx.get().setPacketHandled(true);
    }
}