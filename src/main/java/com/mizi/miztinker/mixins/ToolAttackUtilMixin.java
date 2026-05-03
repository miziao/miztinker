package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

@Mixin(value = ToolAttackUtil.class, remap = false)
public abstract class ToolAttackUtilMixin {

    @ModifyVariable(
            method = "performAttack(Lslimeknights/tconstruct/library/tools/nbt/IToolStackView;Lslimeknights/tconstruct/library/tools/context/ToolAttackContext;)Z",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private static float adjustMinDamage(float damage, IToolStackView tool, ToolAttackContext context) {
        if (tool.getModifierLevel(MiztinkerModifiers.brick.getId()) > 0) {
            return Math.max(damage, 1.0F);
        }
        return damage;
    }
}