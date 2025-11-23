package com.mizi.miztinker;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.model.NBTKeyModel;
import slimeknights.tconstruct.library.tools.SlotType;

public class MusicSlots {
    public static final SlotType MUSIC = SlotType.getOrCreate("music");

    private MusicSlots() {}

    @OnlyIn(Dist.CLIENT)
    public static void init() {
        // registers a custom slot texture for tools
        NBTKeyModel.registerExtraTexture(
                new ResourceLocation("tconstruct:creative_slot"),
                MUSIC.getName(),
                new ResourceLocation("miztinker:item/dx") // your texture path
        );
    }
}