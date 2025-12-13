package com.mizi.miztinker.network.packets.HudCharge;

import com.mizi.miztinker.gui.hudhelder.MurasamaDrawTime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class MurasamaEnergyPointCharge {
    private float energy_point;
    public MurasamaEnergyPointCharge(float energy_point) {;
        this.energy_point =energy_point;
    }
    public MurasamaEnergyPointCharge(FriendlyByteBuf buf){
        this.energy_point =buf.readFloat();
    }

    public static void encode(MurasamaEnergyPointCharge packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.energy_point);
    }

    public static MurasamaEnergyPointCharge decode(FriendlyByteBuf buf) {
        return new MurasamaEnergyPointCharge(buf.readFloat());
    }

    public static void handle(MurasamaEnergyPointCharge packet, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            MurasamaDrawTime.setEnergyPointPercentage(packet.energy_point);
        });
        supplier.get().setPacketHandled(true);
    }
}
