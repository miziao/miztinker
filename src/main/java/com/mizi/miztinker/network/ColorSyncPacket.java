package com.mizi.miztinker.network;

import com.mizi.miztinker.modifier.modifiers.ColorModifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.function.Supplier;

public class ColorSyncPacket {
    private final int r, g, b;

    public ColorSyncPacket(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public ColorSyncPacket(FriendlyByteBuf buffer) {
        this.r = buffer.readInt();
        this.g = buffer.readInt();
        this.b = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(r);
        buffer.writeInt(g);
        buffer.writeInt(b);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (ModifierUtil.getModifierLevel(stack, ColorModifier.ID) > 0) {
                    ToolStack tool = ToolStack.from(stack);
                    tool.getPersistentData().putInt(ColorModifier.ENTITY_COLOR_RED, r);
                    tool.getPersistentData().putInt(ColorModifier.ENTITY_COLOR_GREEN, g);
                    tool.getPersistentData().putInt(ColorModifier.ENTITY_COLOR_BLUE, b);
                }
            }
        });
        context.setPacketHandled(true);
    }
}