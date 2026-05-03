package com.mizi.miztinker.modifier.diadema;

import com.mizi.miztinker.miztinker;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.CsdyRegistries;
import com.mizi.miztinker.modifier.diadema.BanshoTenin.BanshoTeninDiadema;
import com.mizi.miztinker.modifier.diadema.hunter_game.HunterGameDiadema;
import com.mizi.miztinker.modifier.diadema.paper_bomb.TandemPaperBombDiadema;
import com.mizi.miztinker.modifier.diadema.respect_play.RespectPlayDiadema;
import com.mizi.miztinker.modifier.diadema.trinket_hate.TrinketHateDiadema;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import com.mizi.miztinker.modifier.diadema.onimiko.OniMikoDiadema;
import com.mizi.miztinker.modifier.diadema.miziao.MusicGameDiadema;



// 把你的领域注册上来就算是完成了！
    public class DiademaRegister {

    public static final DeferredRegister<DiademaType> DIADEMA_TYPES =
            DeferredRegister.create(CsdyRegistries.DIADEMA_TYPE, miztinker.MODID);

    public static final RegistryObject<DiademaType> ONIMIKO =
            DIADEMA_TYPES.register("onimiko", () -> DiademaType.create(OniMikoDiadema::new));

    public static final RegistryObject<DiademaType> MUSICGAME =
            DIADEMA_TYPES.register("musicgamediadema", () -> DiademaType.create(MusicGameDiadema::new));

    public static final RegistryObject<DiademaType> TRINKETHATE =
            DIADEMA_TYPES.register("trinket_hate_diadema", () -> DiademaType.create(TrinketHateDiadema::new));

    public static final RegistryObject<DiademaType> TANDEMPAPERBOMB =
            DIADEMA_TYPES.register("tandem_paper_bomb", () -> DiademaType.create(TandemPaperBombDiadema::new));

    public static final RegistryObject<DiademaType> BANSHOTENIN =
            DIADEMA_TYPES.register("banshotenin", () -> DiademaType.create(BanshoTeninDiadema::new));

    public static final RegistryObject<DiademaType> HUNTERGAME =
            DIADEMA_TYPES.register("hunter_game", () -> DiademaType.create(HunterGameDiadema::new));

    public static final RegistryObject<DiademaType> RESPECT_PLAY =
            DIADEMA_TYPES.register("respect_play", () -> DiademaType.create(RespectPlayDiadema::new));
}




