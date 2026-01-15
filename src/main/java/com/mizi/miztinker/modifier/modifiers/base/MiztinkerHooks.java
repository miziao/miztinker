package com.mizi.miztinker.modifier.modifiers.base;

import slimeknights.tconstruct.library.module.ModuleHook;
import net.minecraft.resources.ResourceLocation;

public class MiztinkerHooks {

    public static final ModuleHook<EmbossmentModifierHook> EMBOSSMENT = new ModuleHook<>(
            new ResourceLocation("miztinker", "embossment"),
            EmbossmentModifierHook.class,
            EmbossmentModifierHook.AllMerger::new,
            new EmbossmentModifierHook.DefaultClass()
    );
}