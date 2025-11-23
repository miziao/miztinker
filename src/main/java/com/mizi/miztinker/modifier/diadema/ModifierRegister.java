package com.mizi.miztinker.modifier.diadema;


import com.csdy.tcondiadema.modifier.CommonDiademaModifier;
import com.csdy.tcondiadema.modifier.DiademaModifier;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

import static com.mizi.miztinker.miztinker.MODID;

public class ModifierRegister {
    public static ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(MODID);
    private static boolean LOADED = false;

    static {
        try {
            // 检测两个类是否都存在
            Class.forName("com.csdy.tcondiadema.modifier.CommonDiademaModifier");
            LOADED = true;
        } catch (Throwable ignored) {
            LOADED = false;
        }
    }

    public static final StaticModifier<DiademaModifier> ONIMIKO_STATIC_MODIFIER =
            MODIFIERS.register("onimiko", CommonDiademaModifier.Create(DiademaRegister.ONIMIKO::get));

    public static final StaticModifier<DiademaModifier> MUSICGAME_STATIC_MODIFIER=
            MODIFIERS.register("musicgame", CommonDiademaModifier.Create(DiademaRegister.MUSICGAME::get));
}

