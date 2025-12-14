package com.mizi.miztinker.renderer.murasama;


import net.minecraft.resources.ResourceLocation;

public class OjangUtils {

    public static ResourceLocation newRL(String n_p){
        return new ResourceLocation(n_p);
    }

    public static ResourceLocation newRL(String n, String p){
        return new ResourceLocation(n, p);
    }
}
