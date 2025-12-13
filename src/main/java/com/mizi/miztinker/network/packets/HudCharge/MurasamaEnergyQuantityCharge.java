package com.mizi.miztinker.network.packets.HudCharge;

import com.mizi.miztinker.gui.hudhelder.MurasamaDrawTime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class MurasamaEnergyQuantityCharge {
    private float energy_quantity;
    public MurasamaEnergyQuantityCharge(float energy_quantity) {
        this.energy_quantity = energy_quantity;
        
    }

    public MurasamaEnergyQuantityCharge(FriendlyByteBuf buf) {
        this.energy_quantity = buf.readFloat();
    }

    public static void encode(MurasamaEnergyQuantityCharge packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.energy_quantity);
    }

    public static MurasamaEnergyQuantityCharge decode(FriendlyByteBuf buf) {
        return new MurasamaEnergyQuantityCharge(buf.readFloat());
    }

    public static void handle(MurasamaEnergyQuantityCharge packet, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            MurasamaDrawTime.setEnergyQuantityPercentage(packet.energy_quantity);
        });
        supplier.get().setPacketHandled(true);
    }
}
