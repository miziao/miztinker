package com.mizi.miztinker.mixins;

import com.mizi.miztinker.client.MizShaderClient;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityRendererMixin {
    @Shadow
    @Final
    public float bobOffs;

    @Inject(method = "getSpin", at = @At("HEAD"), cancellable = true)
    private void miztinker$freezeTimeStoppedSpin(float partialTicks, CallbackInfoReturnable<Float> cir) {
        ItemEntity item = (ItemEntity) (Object) this;
        if (MizShaderClient.isEntityVisuallyStopped(item)) {
            cir.setReturnValue((float) item.getAge() / 20.0F + this.bobOffs);
        }
    }
}
