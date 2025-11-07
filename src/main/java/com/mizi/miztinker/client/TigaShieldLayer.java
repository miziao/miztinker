package com.mizi.miztinker.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class TigaShieldLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation SHIELD_TEXTURE =
            new ResourceLocation("miztinker", "textures/models/tiga_shield.png");

    public TigaShieldLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!TigaShieldRenderTracker.hasShield(player)) return;

        matrixStack.pushPose();

        // 固定在玩家头顶
        matrixStack.translate(0.0, 0.0f, 0.0); // 1.8f 大约玩家头顶高度

        matrixStack.scale(1.1f, 1.1f, 1.0f);

        // 渐变透明闪烁
        float alpha = 0.3f + 0.2f * (float) Math.sin(player.tickCount * 0.2f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, SHIELD_TEXTURE);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(SHIELD_TEXTURE));

        // 渲染全身：头、身体、手臂、腿
        this.getParentModel().head.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 0.85f, 0f, alpha);
        this.getParentModel().body.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 0.85f, 0f, alpha);
        this.getParentModel().leftArm.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 0.85f, 0f, alpha);
        this.getParentModel().rightArm.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 0.85f, 0f, alpha);
        this.getParentModel().leftLeg.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 0.85f, 0f, alpha);
        this.getParentModel().rightLeg.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 0.85f, 0f, alpha);

        RenderSystem.disableBlend();
        matrixStack.popPose();
    }
}