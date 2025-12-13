package com.mizi.miztinker.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.function.Supplier;

import static com.mizi.miztinker.item.tool.murasama.*;
import static com.mizi.miztinker.item.tool.until.MiztinkerTools.murasama;

public class MizitinkerKeyInputPKT {
    public int key;

    public MizitinkerKeyInputPKT(){
    }

    public MizitinkerKeyInputPKT(int key){
        this.key = key;
    }

    public static void encode(MizitinkerKeyInputPKT pkt, FriendlyByteBuf buf){
        buf.writeInt(pkt.key);
    }

    public static MizitinkerKeyInputPKT decode(FriendlyByteBuf buf){
        return new MizitinkerKeyInputPKT(buf.readInt());
    }

    public static void handlePacket(MizitinkerKeyInputPKT pkt, Supplier<NetworkEvent.Context> context$) {
        NetworkEvent.Context context = context$.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player!=null&&(player.getMainHandItem().is(murasama.get()))) {
                ToolStack tool = ToolStack.from(player.getMainHandItem());
                ModDataNBT data = tool.getPersistentData();
                String s = cannot_create_scabbard.toString();
                String s1 = is_smash_down.toString();
                if (!player.getPersistentData().getBoolean(s)
                        &&data.getFloat(ascending_points)>0
                        &&!player.getPersistentData().getBoolean(s1)
//                        &&tool.getPersistentData().getFloat(ascending_cut)==60
                        &&!player.onGround()
                        &&player.getPersistentData().getFloat(murasam_slash_cooldown.toString())==0
                        &&(tool.getPersistentData().getBoolean(tool_murasama_lock_a)||isTrueNameA(tool)||isTrueNameB(tool))){
                    createScabbard(player,1);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
