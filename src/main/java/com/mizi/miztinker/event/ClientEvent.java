package com.mizi.miztinker.event;

import com.mizi.miztinker.key.MiztinkerKey;
import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.packets.MizitinkerKeyInputPKT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.mizi.miztinker.miztinker.MODID;


@Mod.EventBusSubscriber(modid = MODID, value = {Dist.CLIENT})
public class ClientEvent {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (MiztinkerKey.KeyBinding.KEY.consumeClick()) {
            MiztinkerNetwork.sendToServer(new MizitinkerKeyInputPKT());
        }
    }
}
