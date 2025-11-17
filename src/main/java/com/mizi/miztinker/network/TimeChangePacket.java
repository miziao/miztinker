package com.mizi.miztinker.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TimeChangePacket {
    private final int nextState;

    public TimeChangePacket(int nextState) {
        this.nextState = nextState;
    }

    public static void encode(TimeChangePacket msg, net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeInt(msg.nextState);
    }

    public static TimeChangePacket decode(net.minecraft.network.FriendlyByteBuf buf) {
        return new TimeChangePacket(buf.readInt());
    }

    public static void handle(TimeChangePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            ServerLevel server = (ServerLevel) player.level();
            if (!server.dimensionTypeId().equals(BuiltinDimensionTypes.OVERWORLD)) return;

            long newTime = switch (msg.nextState) {
                case 0 -> 0L;
                case 1 -> 6000L;
                case 2 -> 13000L;
                default -> 18000L;
            };

            server.setDayTime(newTime);
        });
        ctx.get().setPacketHandled(true);
    }
}