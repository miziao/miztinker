package com.mizi.miztinker.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class MiztinkerNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("miztinker", "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, TimeChangePacket.class, TimeChangePacket::encode, TimeChangePacket::decode, TimeChangePacket::handle);
        CHANNEL.registerMessage(id++, WeatherChangePacket.class, WeatherChangePacket::encode, WeatherChangePacket::decode, WeatherChangePacket::handle);
    }
}