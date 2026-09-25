package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class DiamondBody extends NoLevelsModifier implements ModifyDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }

    @Override
    public float modifyDamageTaken(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull EquipmentContext context,
            @NotNull EquipmentSlot slot,
            @NotNull DamageSource damageSource,
            float amount,
            boolean isDirectDamage
    ) {
        LivingEntity entity = context.getEntity();
        int level = modifier.getLevel();

        float maxHealth = entity.getMaxHealth();

        float reductionRatio = maxHealth * level * 0.04f;

        if (reductionRatio > 0.8f) {
            reductionRatio = 0.8f;
        }

        float finalAmount = amount * (1.0f - reductionRatio);

        return Math.max(finalAmount, 0f);
    }
}