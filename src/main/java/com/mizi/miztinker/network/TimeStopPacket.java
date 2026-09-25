package com.mizi.miztinker.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TimeStopPacket {
    private final boolean active;
    private final UUID controllerId;

    public TimeStopPacket(boolean active) {
        this(active, null);
    }

    public TimeStopPacket(boolean active, UUID controllerId) {
        this.active = active;
        this.controllerId = controllerId;
    }

    public static void encode(TimeStopPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
        buf.writeBoolean(msg.controllerId != null);
        if (msg.controllerId != null) {
            buf.writeUUID(msg.controllerId);
        }
    }

    public static TimeStopPacket decode(FriendlyByteBuf buf) {
        boolean active = buf.readBoolean();
        UUID controllerId = buf.readBoolean() ? buf.readUUID() : null;
        return new TimeStopPacket(active, controllerId);
    }

    public static void handle(TimeStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.setTimeStopActive(msg.active, msg.controllerId));
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static class ClientHandler {
        private static void setTimeStopActive(boolean active, UUID controllerId) {
            com.mizi.miztinker.client.MizShaderClient.setTimeStopActive(active, controllerId);
        }
    }
}
