package com.mizi.miztinker.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.function.Supplier;

public class WeatherChangePacket {
    private static final ResourceLocation WEATHER_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "bornofstorm.weather");

    private final int weatherState;
    private final int slotId;

    public WeatherChangePacket(int weatherState, int slotId) {
        this.weatherState = weatherState;
        this.slotId = slotId;
    }

    public static void encode(WeatherChangePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.weatherState);
        buf.writeInt(msg.slotId);
    }

    public static WeatherChangePacket decode(FriendlyByteBuf buf) {
        return new WeatherChangePacket(buf.readInt(), buf.readInt());
    }


    public static void handle(WeatherChangePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (player.level() instanceof ServerLevel server) {
                switch (msg.weatherState) {
                    case 0 -> server.setWeatherParameters(12000, 0, false, false);
                    case 1 -> server.setWeatherParameters(0, 12000, true, false);
                    case 2 -> server.setWeatherParameters(0, 12000, true, true);
                }
            }

            if (msg.slotId >= 0 && msg.slotId < player.containerMenu.slots.size()) {
                ItemStack stack = player.containerMenu.getSlot(msg.slotId).getItem();

                if (!stack.isEmpty() && stack.getOrCreateTag().contains("tic_stats")) {
                    ToolStack tool = ToolStack.copyFrom(stack);

                    tool.getPersistentData().putInt(WEATHER_KEY, msg.weatherState);
                    tool.updateStack(stack);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}