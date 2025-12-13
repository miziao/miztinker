package com.mizi.miztinker.modifier.diadema;

import com.mizi.miztinker.miztinker;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.CsdyRegistries;
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
}




