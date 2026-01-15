package com.mizi.miztinker.modifier.register;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MiztinkerPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, "miztinker");

    public static final RegistryObject<Potion> STRENGTH_OLD_POTION = POTIONS.register("strength_old_potion",
            () -> new Potion(new MobEffectInstance(MiztinkerEffect.StrengthOldEffect.get(), 3600, 0)));

    public static final RegistryObject<Potion> STRENGTH_OLD_POTION_LONG = POTIONS.register("strength_old_potion_long",
            () -> new Potion(new MobEffectInstance(MiztinkerEffect.StrengthOldEffect.get(), 9600, 0)));

    public static final RegistryObject<Potion> STRENGTH_OLD_POTION_STRONG = POTIONS.register("strength_old_potion_strong",
            () -> new Potion(new MobEffectInstance(MiztinkerEffect.StrengthOldEffect.get(), 1800, 1)));
}