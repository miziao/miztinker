package com.mizi.miztinker.modifier.modifiers.base;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * 通用减伤特性
 * reduction = 0.07f → 7% 减伤
 * reduction = 1.0f  → 100% 减伤
 */
public class DamageReductionModifier extends NoLevelsModifier
        implements ModifyDamageModifierHook {

    /** 减伤率（0~1） */
    private final float reduction;

    public DamageReductionModifier(float reduction) {
        this.reduction = Math.min(Math.max(reduction, 0f), 1f);
    }

    @Override
    public float modifyDamageTaken(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry entry,
            @NotNull EquipmentContext context,
            @NotNull EquipmentSlot slot,
            @NotNull DamageSource damageSource,
            float amount,
            boolean isDirectDamage
    ) {
        return amount * (1.0f - reduction);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }
}
