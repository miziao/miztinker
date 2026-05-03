package com.mizi.miztinker.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class Pair_Kamui_effect extends MobEffect {
    public static final ThreadLocal<Boolean> BYPASS_THREAD_LOCAL = ThreadLocal.withInitial(() -> false);

    public Pair_Kamui_effect() {
        super(MobEffectCategory.BENEFICIAL, 0x3E4447);
    }
}