package com.mizi.miztinker.network;

import com.mizi.miztinker.network.packets.PlaySoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class MiztinkerNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("miztinker", "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
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
