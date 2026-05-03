package com.mizi.miztinker.network;

import com.mizi.miztinker.client.MizShaderClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class TimeStopPacket {
    private final boolean active;

    public TimeStopPacket(boolean active) {
        this.active = active;
    }

    public static void encode(TimeStopPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
    }

    public static TimeStopPacket decode(FriendlyByteBuf buf) {
        return new TimeStopPacket(buf.readBoolean());
    }

    public static void handle(TimeStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            MizShaderClient.setShaderActive(msg.active);
        });
        ctx.get().setPacketHandled(true);
    }
}