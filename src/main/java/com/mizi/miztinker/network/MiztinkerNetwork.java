package com.mizi.miztinker.network;

import com.mizi.miztinker.network.packets.HudCharge.MurasamaEnergyPointCharge;
import com.mizi.miztinker.network.packets.HudCharge.MurasamaEnergyQuantityCharge;
import com.mizi.miztinker.network.packets.MizitinkerKeyInputPKT;
import com.mizi.miztinker.network.packets.PlaySoundPacket;
import com.mizi.miztinker.network.packets.ScabbardPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.mizi.miztinker.miztinker.getResource;

public class MiztinkerNetwork {
    private static int packetId = 0; // 统一使用这个计数器

    public static final SimpleChannel INSTANCE =
            NetworkRegistry.newSimpleChannel(
                    getResource("packet"),
                    () -> "1",
                    "1"::equals,
                    "1"::equals
            );

    private static int id() {
        return packetId++;
    }

    public static void init() {
        INSTANCE.messageBuilder(ScabbardPacket.class, id(), NetworkDirection.PLAY_TO_SERVER).decoder(ScabbardPacket::decode).encoder(ScabbardPacket::encode).consumerMainThread(ScabbardPacket::handlePacket).add();
        INSTANCE.messageBuilder(MizitinkerKeyInputPKT.class, id(), NetworkDirection.PLAY_TO_SERVER).decoder(MizitinkerKeyInputPKT::decode).encoder(MizitinkerKeyInputPKT::encode).consumerMainThread(MizitinkerKeyInputPKT::handlePacket).add();

        INSTANCE.messageBuilder(MurasamaEnergyQuantityCharge.class, id(), NetworkDirection.PLAY_TO_CLIENT).decoder(MurasamaEnergyQuantityCharge::new).encoder(MurasamaEnergyQuantityCharge::encode).consumerMainThread(MurasamaEnergyQuantityCharge::handle).add();
        INSTANCE.messageBuilder(MurasamaEnergyPointCharge.class, id(), NetworkDirection.PLAY_TO_CLIENT).decoder(MurasamaEnergyPointCharge::new).encoder(MurasamaEnergyPointCharge::encode).consumerMainThread(MurasamaEnergyPointCharge::handle).add();

        INSTANCE.messageBuilder(PlaySoundPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT).decoder(PlaySoundPacket::decode).encoder(PlaySoundPacket::encode).consumerMainThread(PlaySoundPacket::handle).add();
    }

    public static <MSG> void sendToServer(MSG msg){
        INSTANCE.sendToServer(msg);
    }

    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static <MSG> void sendToNear(MSG msg, net.minecraft.server.level.ServerLevel level, net.minecraft.world.phys.Vec3 pos, double range) {
        INSTANCE.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.x, pos.y, pos.z, range, level.dimension())), msg);
    }

    public static <MSG> void sendToClient(MSG msg){
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}