package com.mizi.miztinker.client;

import com.mizi.miztinker.modifier.modifiers.ColorModifier;
import com.mizi.miztinker.network.ColorSyncPacket;
import com.mizi.miztinker.network.MiztinkerNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "miztinker")
public class ColorCaptureHandler {

    @SubscribeEvent
    public static void onClientAttack(AttackEntityEvent event) {
        if (event.getEntity().level.isClientSide && event.getEntity() == Minecraft.getInstance().player) {
            if (event.getTarget() instanceof LivingEntity target) {
                ItemStack stack = event.getEntity().getMainHandItem();

                if (ModifierUtil.getModifierLevel(stack, ColorModifier.ID) > 0) {
                    int[] rgb = getEntityColor(target);

                    MiztinkerNetwork.INSTANCE.sendToServer(new ColorSyncPacket(rgb[0], rgb[1], rgb[2]));
                }
            }
        }
    }

    public static int[] getEntityColor(LivingEntity entity) {
        if (entity == null || entity instanceof EnderDragon) return new int[]{0, 0, 0};

        try {
            EntityRenderer<? super LivingEntity> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            ResourceLocation texture = renderer.getTextureLocation(entity);

            return Minecraft.getInstance().getResourceManager().getResource(texture).map(resource -> {
                try (InputStream stream = resource.open()) {
                    BufferedImage image = ImageIO.read(stream);
                    return image != null ? calculateAverageColor(image) : new int[]{255, 255, 255};
                } catch (Exception e) {
                    return new int[]{255, 255, 255};
                }
            }).orElse(new int[]{255, 255, 255});

        } catch (Exception ignored) {}
        return new int[]{255, 255, 255};
    }

    private static int[] calculateAverageColor(BufferedImage image) {
        long r = 0, g = 0, b = 0;
        int count = 0;
        int step = 8;
        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                int argb = image.getRGB(x, y);
                if (((argb >> 24) & 0xFF) > 128) {
                    r += (argb >> 16) & 0xFF;
                    g += (argb >> 8) & 0xFF;
                    b += argb & 0xFF;
                    count++;
                }
            }
        }
        return count == 0 ? new int[]{255, 255, 255} : new int[]{(int)(r/count), (int)(g/count), (int)(b/count)};
    }
}