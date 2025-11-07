package com.mizi.miztinker.modifier.diadema.onimiko;

import com.csdy.tcondiadema.frames.diadema.ClientDiadema;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 实现请看com.csdy.tcondiadema.clientHaloRender
 */
@OnlyIn(Dist.CLIENT)
public class OniMikoClientDiadema extends ClientDiadema {

    private static boolean LOADED = false;

    static {
        try {
            // 检测两个类是否都存在
            Class.forName("com.csdy.tcondiadema.frames.diadema.ClientDiadema");
            LOADED = true;
        } catch (Throwable ignored) {
            LOADED = false;
        }
    }
}
