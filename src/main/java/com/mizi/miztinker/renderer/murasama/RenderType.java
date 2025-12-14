package com.mizi.miztinker.renderer.murasama;


import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;

import static com.mizi.miztinker.miztinker.MODID;

@OnlyIn(Dist.CLIENT)
public class RenderType {

    private static int bloomIdx = 0;
    public static final HashMap<ResourceLocation, UltimateSlashRenderType> BloomRenderTypes = Maps.newHashMap();
    public static UltimateSlashRenderType getBloomRenderTypeByTexture(ResourceLocation texture){
        if(BloomRenderTypes.containsKey(texture)){
            return BloomRenderTypes.get(texture);
        }
        else {
            UltimateSlashRenderType bloomType = new UltimateSlashRenderType(OjangUtils.newRL(MODID, "bp_" + bloomIdx++), texture);
            BloomRenderTypes.put(texture, bloomType);
            return bloomType;
        }
    }
}
