package com.mizi.miztinker.renderer;

import com.mizi.miztinker.entity.ScabbardEntity.ScabbardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public class ScabbardRenderer extends EntityRenderer<ScabbardEntity> {
    public ItemRenderer itemRenderer;
    public ScabbardRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        this.itemRenderer = p_174008_.getItemRenderer();
    }


    @Override
    public void render(ScabbardEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entity.tickCount >= 0 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
            matrixStackIn.pushPose();

            float spawnYaw = entity.getSpawnYaw();
            float spawnPitch = entity.getSpawnPitch();

            matrixStackIn.mulPose(Axis.YP.rotationDegrees(-spawnYaw));
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(spawnPitch));
            matrixStackIn.translate(0, entity.getBbHeight() * 0.5F, 0);

            if (entity.isRotating()) {
                float rotationAngle = (entity.tickCount + partialTicks) * 60F;
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(rotationAngle));
            }

            matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0F));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(135.0F));

            matrixStackIn.translate(-0.03125, -0.09375, 0);
            matrixStackIn.scale(1.5F, 1.5F, 1.5F);
            this.itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, entity.level(), entity.getId());
            matrixStackIn.popPose();
            super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        }
    }
    public int getSkyLightLevel(ScabbardEntity p_114509_, BlockPos p_114510_) {
        return 15;
    }
    public int getBlockLightLevel(ScabbardEntity p_114496_, BlockPos p_114497_) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(ScabbardEntity meteorEntity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
