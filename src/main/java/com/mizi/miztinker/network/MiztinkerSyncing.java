package com.mizi.miztinker.network;


import com.mizi.miztinker.network.packets.PlaySoundPacket;
import com.mizi.miztinker.miztinker;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = miztinker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MiztinkerSyncing {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(miztinker.MODID, "miztinker"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );


    public static void Init() {
        int packetId = 0;
        CHANNEL.registerMessage(
                packetId++,
                PlaySoundPacket.class,
                PlaySoundPacket::encode,
                PlaySoundPacket::decode,
                PlaySoundPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT) // 明确指定方向
        );

    }
}
