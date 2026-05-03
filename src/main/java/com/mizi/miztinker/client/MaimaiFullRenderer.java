package com.mizi.miztinker.client;

import com.mizi.miztinker.block.MaimaiFullBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class MaimaiFullRenderer extends GeoBlockRenderer<MaimaiFullBlockEntity> {
    public MaimaiFullRenderer() {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath("miztinker", "maimai_full")));
    }
}