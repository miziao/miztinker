package com.mizi.miztinker.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class TigaShieldLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation SHIELD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("miztinker", "textures/models/tiga_shield.png");

    public TigaShieldLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (!TigaShieldRenderTracker.hasShield(player)) return;

        matrixStack.pushPose();

        matrixStack.scale(1.05f, 1.05f, 1.05f);

        float alpha = 0.3f + 0.2f * (float) Math.sin((player.tickCount + partialTicks) * 0.2f);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(SHIELD_TEXTURE));

        this.getParentModel().head.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha);
        this.getParentModel().body.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha);
        this.getParentModel().leftArm.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha);
        this.getParentModel().rightArm.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha);
        this.getParentModel().leftLeg.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha);
        this.getParentModel().rightLeg.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha);

        matrixStack.popPose();
    }
}