package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Ironman extends NoLevelsModifier implements ModifyDamageModifierHook {

    @Override
    public float modifyDamageTaken(IToolStackView tool,
                                   ModifierEntry entry,
                                   EquipmentContext context,
                                   EquipmentSlot slot,
                                   DamageSource source,
                                   float amount,
                                   boolean isDirectDamage) {
        LivingEntity entity = context.getEntity();
        if (entity != null) {
            // 获取护甲值
            float armor = entity.getArmorValue();
            // 计算减伤比例
            float reductionRate = armor * 0.001f; // 每点护甲 0.1% -> 0.001
            if (reductionRate > 0.8f) {
                // 可选：设个上限，比如最多减少 80%
                reductionRate = 0.95f;
            }
            return amount * (1 - reductionRate);
        }
        return amount;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }
}
