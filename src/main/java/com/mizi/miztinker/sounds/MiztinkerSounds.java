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
            () -> SoundEvent.createVariableRangeEvent( ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "doom_guy")));

    public static final RegistryObject<SoundEvent> ULTRAMAN = SOUND_EVENTS.register("ultraman",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "ultraman")));

    public static final RegistryObject<SoundEvent> GUANYU = SOUND_EVENTS.register("guanyu",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "guanyu")));

    public static final RegistryObject<SoundEvent> MARIO = SOUND_EVENTS.register("mario",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "mario")));

    public static final RegistryObject<SoundEvent> KNIGHT_OF_NIGHT = SOUND_EVENTS.register("knight_of_night",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "knight_of_night")));

    public static final RegistryObject<SoundEvent> DISCONNECTED = SOUND_EVENTS.register("disconnected",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "disconnected")));

    public static final RegistryObject<SoundEvent> UMIYURI_KAITEITAN = SOUND_EVENTS.register("umiyuri_kaiteitan",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "umiyuri_kaiteitan")));

    public static final RegistryObject<SoundEvent> HHHHA = SOUND_EVENTS.register("hhhha",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "hhhha")));

    public static final RegistryObject<SoundEvent> KAMUI = SOUND_EVENTS.register("kamui",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "kamui")));

    public static final RegistryObject<SoundEvent> SIGMA = SOUND_EVENTS.register("sigma",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "sigma")));

    public static final RegistryObject<SoundEvent> ALL_JUSTICE = SOUND_EVENTS.register("all_justice",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "all_justice")));
}

