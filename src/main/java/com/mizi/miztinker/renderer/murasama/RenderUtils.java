package com.mizi.miztinker.renderer.murasama;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static com.mizi.miztinker.miztinker.MODID;

public class RenderUtils {
    public static void GLSetTexture(ResourceLocation texture){
        TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstracttexture = texturemanager.getTexture(texture);
        RenderSystem.bindTexture(abstracttexture.getId());
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.setShaderTexture(0, abstracttexture.getId());
    }

    public static ResourceLocation GetTexture(String path){
        return OjangUtils.newRL(MODID, "textures/" + path + ".png");
    }

    public static final int EmissiveLightPos = 15728880;

    @OnlyIn(Dist.CLIENT)
    public static void AddParticle(ClientLevel level, Particle particle){
        try {
            Minecraft mc  = Minecraft.getInstance();
            Camera camera = mc.gameRenderer.getMainCamera();
            if (camera.isInitialized()) {
                if (camera.getPosition().distanceToSqr(particle.x, particle.y, particle.z) < 1024.0D) {
                    mc.particleEngine.add(particle);
                }
            }
        }catch (Exception e){

        }
    }
}
