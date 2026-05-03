package com.mizi.miztinker.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class ClientOnlyUtils {

    public static int[] getEntityColorFromClient(LivingEntity entity) {
        System.out.println("[ColorDebug] Start sampling color for: " + entity.getName().getString());
        try {
            EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            ResourceLocation texture = renderer.getTextureLocation(entity);

            System.out.println("[ColorDebug] Found texture location: " + texture.toString());

            return Minecraft.getInstance().getResourceManager().getResource(texture).map(resource -> {
                try (InputStream stream = resource.open()) {
                    BufferedImage image = ImageIO.read(stream);
                    if (image == null) {
                        System.out.println("[ColorDebug] ERROR: BufferedImage is NULL. ImageIO failed.");
                        return new int[]{0, 0, 0};
                    }

                    System.out.println("[ColorDebug] Texture loaded: " + image.getWidth() + "x" + image.getHeight());
                    return calculateAverageColor(image);
                } catch (Exception e) {
                    System.out.println("[ColorDebug] EXCEPTION while opening stream: " + e.getMessage());
                    return new int[]{0, 0, 0};
                }
            }).orElseGet(() -> {
                System.out.println("[ColorDebug] ERROR: ResourceLocation not found in Resource Manager.");
                return new int[]{0, 0, 0};
            });
        } catch (Exception e) {
            System.out.println("[ColorDebug] CRITICAL ERROR in ClientOnlyUtils: " + e.getMessage());
            e.printStackTrace();
            return new int[]{0, 0, 0};
        }
    }

    private static int[] calculateAverageColor(BufferedImage image) {
        final int sampleStep = 8;
        long r = 0, g = 0, b = 0;
        int count = 0;
        for (int y = 0; y < image.getHeight(); y += sampleStep) {
            for (int x = 0; x < image.getWidth(); x += sampleStep) {
                int argb = image.getRGB(x, y);
                // 仅统计非透明像素
                if (((argb >> 24) & 0xFF) > 128) {
                    r += (argb >> 16) & 0xFF;
                    g += (argb >> 8) & 0xFF;
                    b += argb & 0xFF;
                    count++;
                }
            }
        }
        if (count == 0) {
            System.out.println("[ColorDebug] No non-transparent pixels found in the sample.");
            return new int[]{255, 255, 255};
        }
        return new int[]{(int)(r/count), (int)(g/count), (int)(b/count)};
    }
}