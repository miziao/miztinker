package com.mizi.miztinker.mixins;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.tables.client.inventory.ToolTableScreen;

@Mixin(ToolTableScreen.class)
public abstract class ToolTableScreenMixin {

    @Shadow
    protected boolean enableArmorStandPreview;

    @Inject(method = "init", at = @At("HEAD"))
    private void disableArmorStand(CallbackInfo ci) {
        // 直接禁止生成 ArmorStand
        this.enableArmorStandPreview = false;
    }
}