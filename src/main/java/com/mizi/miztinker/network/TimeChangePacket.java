package com.mizi.miztinker.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public record TimeChangePacket(int nextState) {

    public static void encode(TimeChangePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.nextState());
    }

    public static TimeChangePacket decode(FriendlyByteBuf buf) {
        return new TimeChangePacket(buf.readInt());
    }

    public static void handle(TimeChangePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (player.level() instanceof ServerLevel serverLevel &&
                    serverLevel.dimensionTypeId().equals(BuiltinDimensionTypes.OVERWORLD)) {

                long newTime = switch (msg.nextState()) {
                    case 1 -> 6000L;
                    case 2 -> 13000L;
                    case 3 -> 18000L;
                    default -> 0L;
                };

                serverLevel.setDayTime(newTime);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}