package com.mizi.miztinker.entity.boss.model;



import com.mizi.miztinker.entity.boss.entity.MiziAo;
import com.mizi.miztinker.miztinker;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MiziAoModel extends GeoModel<MiziAo> {
    private final ResourceLocation model = new ResourceLocation(miztinker.MODID, "geo/mizi_ao.geo.json");
    private final ResourceLocation texture = new ResourceLocation(miztinker.MODID, "textures/entity/mizi_ao.png");
    private final ResourceLocation animations = new ResourceLocation(miztinker.MODID, "animations/mizi_ao.animation.json");

    @Override
    public ResourceLocation getModelResource(MiziAo miziAo) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(MiziAo miziAo) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(MiziAo miziAo) {
        return animations;
    }
}
