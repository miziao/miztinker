package com.mizi.miztinker.modifier.hook;

import com.mizi.miztinker.modifier.modifiers.base.EmbossmentModifierHook;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.module.ModuleHook;
import net.minecraft.resources.ResourceLocation;

public class MiztinkerHooks {

    public static final ModuleHook<EmbossmentModifierHook> EMBOSSMENT = new ModuleHook<>(
            ResourceLocation.fromNamespaceAndPath("miztinker", "embossment"),
            EmbossmentModifierHook.class,
            EmbossmentModifierHook.AllMerger::new,
            new EmbossmentModifierHook.DefaultClass()
    );

    public static final ModuleHook<LeftClickModifierHook> LEFT_CLICK = ModifierHooks.register(
            ResourceLocation.fromNamespaceAndPath("miztinker", "left_click"),
            LeftClickModifierHook.class,
            LeftClickModifierHook.AllMerger::new,
            new LeftClickModifierHook() {}
    );

    public static final ModuleHook<ArrowDamageModifierHook> ARROW_DAMAGE = new ModuleHook<>(
            ResourceLocation.fromNamespaceAndPath("miztinker", "arrow_damage"),
            ArrowDamageModifierHook.class,
            ArrowDamageModifierHook.AllMerger::new,
            new ArrowDamageModifierHook.DefaultClass()
    );

}