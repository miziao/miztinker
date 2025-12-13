package com.mizi.miztinker.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.mizi.miztinker.item.tool.murasama.*;


public class ScabbardPacket {
    public final int playerID;
    public ScabbardPacket(int id) {
        this.playerID =id;
    }

    public static void encode(ScabbardPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.playerID);
    }

    public static ScabbardPacket decode(FriendlyByteBuf buf) {
        return new ScabbardPacket(buf.readInt());
    }

    public static void handlePacket(ScabbardPacket packet, Supplier<NetworkEvent.Context> supplier) {
        if (supplier.get().getDirection().getReceptionSide().isServer()) {
            supplier.get().enqueueWork(() -> {
                ServerPlayer player =supplier.get().getSender();
                if (player !=null&&player.getId()==packet.playerID) {
                    if (CanCreateScabbard(player)
                            &&!player.getPersistentData().getBoolean(is_smash_down.toString())) {
                        player.getPersistentData().putBoolean(can_cut_entity.toString(), true);
                    }
                }
            });
        }
        supplier.get().setPacketHandled(true);
    }
}
