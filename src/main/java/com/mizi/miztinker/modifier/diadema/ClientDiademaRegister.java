package com.mizi.miztinker.modifier.diadema;

import com.csdy.tcondiadema.frames.CsdyRegistries;
import com.csdy.tcondiadema.frames.diadema.ClientDiademaType;
import com.mizi.miztinker.modifier.diadema.BanshoTenin.BanshoTeninClientDiadema;
import com.mizi.miztinker.modifier.diadema.hunter_game.HunterGameClientDiadema;
import com.mizi.miztinker.modifier.diadema.paper_bomb.TandemPaperBombClientDiadema;
import com.mizi.miztinker.modifier.diadema.respect_play.RespectPlayClientDiadema;
import com.mizi.miztinker.modifier.diadema.trinket_hate.TrinketHateClientDiadema;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.diadema.onimiko.OniMikoClientDiadema;
import com.mizi.miztinker.modifier.diadema.miziao.MusicGameClientDiadema;

@OnlyIn(Dist.CLIENT)
public class ClientDiademaRegister {

    public static final DeferredRegister<ClientDiademaType> CLIENT_DIADEMA_TYPES =
            DeferredRegister.create(CsdyRegistries.CLIENT_DIADEMA_TYPE, miztinker.MODID);

    public static final RegistryObject<ClientDiademaType> ONIMIKO =
            CLIENT_DIADEMA_TYPES.register("onimiko", () -> ClientDiademaType.Create(OniMikoClientDiadema::new));

    public static final RegistryObject<ClientDiademaType> MUSICGAME =
            CLIENT_DIADEMA_TYPES.register("musicgamediadema", () -> ClientDiademaType.Create(MusicGameClientDiadema::new));

    public static final RegistryObject<ClientDiademaType> TRINKETHATE =
            CLIENT_DIADEMA_TYPES.register("trinket_hate_diadema", () -> ClientDiademaType.Create(TrinketHateClientDiadema::new));

    public static final RegistryObject<ClientDiademaType> TANDEMPAPERBOMB =
            CLIENT_DIADEMA_TYPES.register("tandem_paper_bomb", () -> ClientDiademaType.Create(TandemPaperBombClientDiadema::new));

    public static final RegistryObject<ClientDiademaType> BANSHOTENIN =
            CLIENT_DIADEMA_TYPES.register("banshotenin", () -> ClientDiademaType.Create(BanshoTeninClientDiadema::new));

    public static final RegistryObject<ClientDiademaType> HUNTERGAME =
            CLIENT_DIADEMA_TYPES.register("hunter_game", () -> ClientDiademaType.Create(HunterGameClientDiadema::new));

    public static final RegistryObject<ClientDiademaType> RESPECT_PLAY =
            CLIENT_DIADEMA_TYPES.register("respect_play", () -> ClientDiademaType.Create(RespectPlayClientDiadema::new));

}