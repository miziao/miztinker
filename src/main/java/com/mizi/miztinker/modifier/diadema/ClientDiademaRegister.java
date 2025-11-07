package com.mizi.miztinker.modifier.diadema;

import com.csdy.tcondiadema.frames.CsdyRegistries;
import com.csdy.tcondiadema.frames.diadema.ClientDiademaType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.diadema.onimiko.OniMikoClientDiadema;

@OnlyIn(Dist.CLIENT)
public class ClientDiademaRegister {
    private static boolean LOADED = false;

    static {
        try {
            // 检测两个类是否都存在
            Class.forName("csdy.tcondiadema.frames.CsdyRegistries");
            LOADED = true;
        } catch (Throwable ignored) {
            LOADED = false;
        }
    }

    public static final DeferredRegister<ClientDiademaType> CLIENT_DIADEMA_TYPES =
            DeferredRegister.create(CsdyRegistries.CLIENT_DIADEMA_TYPE, miztinker.MODID);

    public static final RegistryObject<ClientDiademaType> ONIMIKO =
            CLIENT_DIADEMA_TYPES.register("onimiko", () -> ClientDiademaType.Create(OniMikoClientDiadema::new));

}