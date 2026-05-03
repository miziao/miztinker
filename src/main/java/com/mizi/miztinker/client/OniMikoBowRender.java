package com.mizi.miztinker.client;

import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.diadema.DiademaRegister;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import static com.mizi.miztinker.client.DiademaRenderType.ONIMIKO_BOW_RENDER;

@OnlyIn(Dist.CLIENT)
public class OniMikoBowRender extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation BOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(miztinker.MODID, "textures/models/oni_miko_bow.png");

    private static final float MODEL_TO_WORLD = 1.0F / 16.0F;

    public OniMikoBowRender(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack pose, @NotNull MultiBufferSource renderer, int light,
                       @NotNull AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (player.isInvisible() || !isWearingOniMiko(player))
            return;

        pose.pushPose();

        ModelPart head = this.getParentModel().head;

        pose.translate(head.x * MODEL_TO_WORLD, head.y * MODEL_TO_WORLD, head.z * MODEL_TO_WORLD);

        if (head.zRot != 0.0F) pose.mulPose(Axis.ZP.rotation(head.zRot));
        if (head.yRot != 0.0F) pose.mulPose(Axis.YP.rotation(head.yRot));
        if (head.xRot != 0.0F) pose.mulPose(Axis.XP.rotation(head.xRot));
        float headTopY_modelUnits = 8.0F;
        float haloYOffset_modelUnits = -7.0F;
        float finalY_world = (headTopY_modelUnits + haloYOffset_modelUnits) * MODEL_TO_WORLD;
        float headBackZ_world = 0.25F;

        pose.translate(0.0F, finalY_world, headBackZ_world);

        pose.mulPose(Axis.XP.rotationDegrees(5.0F));
        pose.scale(0.8F, 0.8F, 0.8F);

        VertexConsumer vertex = renderer.getBuffer(ONIMIKO_BOW_RENDER.apply(BOW_TEXTURE));
        Matrix4f matrix = pose.last().pose();

        float s = 0.35F;
        int color = 0xFFFFFFFF;
        float[] uv = new float[]{0.0F, 1.0F, 1.0F, 0.0F};

        vertex.vertex(matrix, -s, -s, 0.0F).color(color).uv(uv[0], uv[1]).uv2(light).endVertex();
        vertex.vertex(matrix, s, -s, 0.0F).color(color).uv(uv[2], uv[1]).uv2(light).endVertex();
        vertex.vertex(matrix, s, s, 0.0F).color(color).uv(uv[2], uv[3]).uv2(light).endVertex();
        vertex.vertex(matrix, -s, s, 0.0F).color(color).uv(uv[0], uv[3]).uv2(light).endVertex();

        vertex.vertex(matrix, -s, s, 0.0F).color(color).uv(uv[0], uv[3]).uv2(light).endVertex();
        vertex.vertex(matrix, s, s, 0.0F).color(color).uv(uv[2], uv[3]).uv2(light).endVertex();
        vertex.vertex(matrix, s, -s, 0.0F).color(color).uv(uv[2], uv[1]).uv2(light).endVertex();
        vertex.vertex(matrix, -s, -s, 0.0F).color(color).uv(uv[0], uv[1]).uv2(light).endVertex();

        pose.popPose();
    }

    private boolean isWearingOniMiko(Player player) {
        return DiademaRegister.ONIMIKO.get().isAffected(player);
    }
}