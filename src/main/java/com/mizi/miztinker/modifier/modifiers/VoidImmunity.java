package com.mizi.miztinker.modifier.modifiers;


import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class VoidImmunity extends NoLevelsModifier implements DamageBlockModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
    }

    /** 匠魂钩子：阻挡虚空伤害 */
    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry,
                                   EquipmentContext context, EquipmentSlot slot,
                                   DamageSource source, float amount) {

        // 获取受伤实体
        context.getEntity();
        context.getEntity();
        LivingEntity entity = context.getEntity();

        // 获取虚空伤害实例
        DamageSource fellOut = entity.level().damageSources().fellOutOfWorld();

        // 判断是否虚空伤害
        return source == fellOut;




    }
}