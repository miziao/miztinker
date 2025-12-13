package com.mizi.miztinker.recipes;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;

@Mod.EventBusSubscriber(modid = "miztinker", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModWandererTrades {

    @SubscribeEvent
    public static void onWanderingTrader(WandererTradesEvent event) {

        event.getRareTrades().addAll(
                Collections.nCopies(3, WanderingNetheriteCoinTrade.INSTANCE)
        );

    }
}
