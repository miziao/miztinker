package com.mizi.miztinker.renderer.other;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class PostPasses {

    public static PostPassBase blit;
    public static DownSampling downSampler;
    public static UpSampling upSampler;
    public static UnityComposite unity_composite;

    public static void register(RegisterShadersEvent event){
        try {
            System.out.println("Load Shader");
            ResourceManager rm = Minecraft.getInstance().getResourceManager();
            blit = new PostPassBase("miztinker:blit",rm);

            downSampler = new DownSampling("miztinker:down_sampling",rm);
            upSampler = new UpSampling("miztinker:up_sampling",rm);
            unity_composite = new UnityComposite("miztinker:unity_composite",rm);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
