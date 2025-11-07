package com.mizi.miztinker.mixins;


import net.minecraft.client.Minecraft; // 目标类：Minecraft 客户端
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class TestMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(GameConfig p_91084_, CallbackInfo ci) {
        System.out.println("测试Mixin生效！Minecraft已初始化");
    }
}