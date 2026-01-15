package com.mizi.miztinker.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;

@Mixin(value = LazyModifier.class, remap = false)
public abstract class LazyModifierMixin {

    @Shadow @Final
    protected ModifierId id;
    @Shadow
    protected Modifier result;

    /**
     * 使用 HEAD 注入，直接模拟原方法逻辑
     * 这样不需要定位具体的 INVOKE 指令，成功率 100%
     */
    @Inject(
            method = "getUnchecked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void suppressMissingModifierError(CallbackInfoReturnable<Modifier> cir) {
        if (this.result == null) {
            this.result = ModifierManager.getValue(this.id);

            if (this.result == ModifierManager.INSTANCE.getDefaultValue() && !ModifierManager.EMPTY.equals(this.id)) {
                cir.setReturnValue(this.result);
            }
        }
    }
}