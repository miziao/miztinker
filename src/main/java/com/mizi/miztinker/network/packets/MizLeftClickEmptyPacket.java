package com.mizi.miztinker.network.packets;

import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MizLeftClickEmptyPacket {
    public MizLeftClickEmptyPacket() {}
    public MizLeftClickEmptyPacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                LeftClickModifierHook.handleLeftClick(stack, player, EquipmentSlot.MAINHAND);
            }
        });
        return true;
    }
}