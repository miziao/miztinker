package com.mizi.miztinker.client;

import com.mizi.miztinker.modifier.modifiers.base.GhostfreakHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GhostfreakRender {

    private static final float ALPHA = 0.45F; // 幽灵透明度

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!GhostfreakHelper.isGhostActive(player)) return;

        event.setCanceled(true);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        PlayerRenderer renderer = event.getRenderer();
        var model = renderer.getModel();
        ResourceLocation skin = renderer.getTextureLocation((AbstractClientPlayer) player);

        var vertex = buffer.getBuffer(RenderType.entityTranslucent(skin));

        poseStack.pushPose();


        float partialTick = event.getPartialTick();

        renderer.setupRotations((AbstractClientPlayer) player, poseStack,
                player.tickCount + partialTick,
                player.yBodyRot,
                partialTick);

        poseStack.translate(0.0D, 1.501122D, 0.0D);
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        float hover = (float) Math.sin((player.tickCount + partialTick) * 0.1F) * 0.05F;
        poseStack.translate(0, hover + 0.05, 0);

        model.renderToBuffer(
                poseStack,
                vertex,
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F,
                ALPHA
        );

        poseStack.popPose();
    }
    }

