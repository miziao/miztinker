package com.mizi.miztinker.network.packets;

import com.mizi.miztinker.modifier.modifiers.EquivalentArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class ShieldSyncPacket {
    private final float currentShield;
    private final int cooldown;

    public ShieldSyncPacket(float currentShield, int cooldown) {
        this.currentShield = currentShield;
        this.cooldown = cooldown;
    }

    public ShieldSyncPacket(FriendlyByteBuf buffer) {
        this.currentShield = buffer.readFloat();
        this.cooldown = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(currentShield);
        buffer.writeInt(cooldown);
    }

    public static void handle(ShieldSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                var nbt = Minecraft.getInstance().player.getPersistentData().getCompound(EquivalentArmor.SHIELD_NBT);
                nbt.putFloat(EquivalentArmor.SHIELD_VAL, msg.currentShield);
                nbt.putInt(EquivalentArmor.SHIELD_COOLDOWN, msg.cooldown);
                Minecraft.getInstance().player.getPersistentData().put(EquivalentArmor.SHIELD_NBT, nbt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}