package com.mizi.miztinker.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraftforge.network.NetworkEvent;

public class S2CSyncDespawn {

    private final int entityId;

    public S2CSyncDespawn(int entityId) {
        this.entityId = entityId;
    }

    public void encode(net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
    }

    public static void send(int entityId) {
        // 直接调用客户端处理（本地单人或测试环境）
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(entityId);
            if (entity != null) {
                entity.remove(RemovalReason.DISCARDED);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> send(entityId));
    }
}