package com.mizi.miztinker.modifier.register;


import com.mizi.miztinker.miztinker;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MiztinkerEffect {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, miztinker.MODID);

    public static final RegistryObject<MobEffect> WoundEffect = EFFECTS.register("woundeffect", com.mizi.miztinker.effect.WoundEffect::new);

    public static final RegistryObject<MobEffect> BoneFractureEffect = EFFECTS.register("bone_fracture", com.mizi.miztinker.effect.BoneFractureEffect::new);

    public static final RegistryObject<MobEffect> StrengthOldEffect = EFFECTS.register("strength_old", com.mizi.miztinker.effect.StrengthOldEffect::new);

    public static final RegistryObject<MobEffect> HorologiumNoAI = EFFECTS.register("horologiumnoai", com.mizi.miztinker.effect.HorologiumNoAI::new);

    public static final RegistryObject<MobEffect> DropFountainEffect = EFFECTS.register("dropfountaineffect", com.mizi.miztinker.effect.DropFountainEffect::new);

    public static final RegistryObject<MobEffect> DestinedDeath = EFFECTS.register("destineddeath", com.mizi.miztinker.effect.DestinedDeath::new);

}
