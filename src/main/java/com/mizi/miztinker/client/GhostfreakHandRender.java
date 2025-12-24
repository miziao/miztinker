package com.mizi.miztinker.client;

import com.mizi.miztinker.modifier.modifiers.base.GhostfreakHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "miztinker",
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class GhostfreakHandRender {

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!GhostfreakHelper.isGhostActive(player)) return;

        event.setCanceled(true);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();

        PlayerRenderer renderer =
                (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);

        float alpha = 0.35F;

        var vertex = buffer.getBuffer(
                RenderType.entityTranslucent(player.getSkinTextureLocation())
        );

        poseStack.pushPose();

        // ===== 关键：调用原版第一人称手渲染 =====
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            renderer.renderRightHand(
                    poseStack,
                    buffer,
                    event.getPackedLight(),
                    player
            );
        } else {
            renderer.renderLeftHand(
                    poseStack,
                    buffer,
                    event.getPackedLight(),
                    player
            );
        }

        poseStack.popPose();
    }
}