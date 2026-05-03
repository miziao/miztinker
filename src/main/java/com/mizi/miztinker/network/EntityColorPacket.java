package com.mizi.miztinker.network;

import com.mizi.miztinker.modifier.modifiers.ColorModifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.function.Supplier;

public class EntityColorPacket {
    private final int r, g, b;

    public EntityColorPacket(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static void encode(EntityColorPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.r);
        buf.writeInt(msg.g);
        buf.writeInt(msg.b);
    }

    public static EntityColorPacket decode(FriendlyByteBuf buf) {
        return new EntityColorPacket(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(EntityColorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                System.out.println("[ColorDebug] Packet RECEIVED by server from: " + player.getScoreboardName());
                System.out.println("[ColorDebug] RGB values in packet: " + msg.r + "," + msg.g + "," + msg.b);

                if (!stack.isEmpty()) {
                    ToolStack tool = ToolStack.from(stack);
                    ModDataNBT data = tool.getPersistentData();

                    data.putInt(ColorModifier.ENTITY_COLOR_RED, msg.r);
                    data.putInt(ColorModifier.ENTITY_COLOR_GREEN, msg.g);
                    data.putInt(ColorModifier.ENTITY_COLOR_BLUE, msg.b);

                    System.out.println("[ColorDebug] SUCCESSFULLY wrote RGB to Tool NBT.");
                } else {
                    System.out.println("[ColorDebug] FAILED to write: Player's main hand is empty.");
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}