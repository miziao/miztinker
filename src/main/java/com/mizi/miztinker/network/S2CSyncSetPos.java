package com.mizi.miztinker.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class S2CSyncSetPos {

    private final int entityId;
    private final double x, y, z;

    public S2CSyncSetPos(int entityId, double x, double y, double z) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void encode(net.minecraft.network.FriendlyByteBuf buffer) {
        buffer.writeInt(entityId).writeDouble(x).writeDouble(y).writeDouble(z);
    }

    public static void send(Entity entity, double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity e = mc.level.getEntity(entity.getId());
            if (e != null) e.setPos(x, y, z);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> send(null, x, y, z));
    }
}
