package com.mizi.miztinker.entity.boss.model;


import com.mizi.miztinker.entity.boss.entity.TitanWarden;
import com.mizi.miztinker.miztinker;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TitanWardenModel extends GeoModel<TitanWarden> {
    private final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "geo/titan_warden.geo.json");
    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "textures/entity/titan_warden.png");
    private final ResourceLocation animations = ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "animations/titan_warden.animation.json");

    @Override
    public ResourceLocation getModelResource(TitanWarden miziAo) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(TitanWarden miziAo) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(TitanWarden miziAo) {
        return animations;
    }
}
