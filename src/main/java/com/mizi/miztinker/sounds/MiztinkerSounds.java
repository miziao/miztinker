package com.mizi.miztinker.sounds;

import com.mizi.miztinker.miztinker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MiztinkerSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, miztinker.MODID);

    public static final RegistryObject<SoundEvent> DOOM_GUY = SOUND_EVENTS.register("doom_guy",
            () -> SoundEvent.createVariableRangeEvent( new ResourceLocation(miztinker.MODID, "doom_guy")));

    public static final RegistryObject<SoundEvent> ULTRAMAN = SOUND_EVENTS.register("ultraman",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(miztinker.MODID, "ultraman")));

    public static final RegistryObject<SoundEvent> GUANYU = SOUND_EVENTS.register("guanyu",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(miztinker.MODID, "guanyu")));

    public static final RegistryObject<SoundEvent> MARIO = SOUND_EVENTS.register("mario",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(miztinker.MODID, "mario")));

    public static final RegistryObject<SoundEvent> KNIGHT_OF_NIGHT = SOUND_EVENTS.register("knight_of_night",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(miztinker.MODID, "knight_of_night")));
}

