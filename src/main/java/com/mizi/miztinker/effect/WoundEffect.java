package com.mizi.miztinker.effect;

import com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurtWithNoHealable;


public class WoundEffect extends MobEffect {

    public WoundEffect() {
        super(MobEffectCategory.HARMFUL, 0x555555);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // 每 tick 执行
    }

    /*@Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

        float current = entity.getHealth();

        // 从 UnhealableEntityData 中读出当前累计伤害 offset
        ForceHurtUtil.UnhealableEntityData data = getData(entity);

        // offset = 向下修正回血量
        // 如果 offset < 当前血量 → 说明这期间产生了回血
        float allowedHealth = current - data.hurtOffset;

        if (current > allowedHealth) {
            float healAmount = current - allowedHealth;

            // 反向伤害相同数值，抵消回血（禁疗核心）
            forceHurtWithNoHealable(entity, entity.damageSources().magic(), healAmount);
        }

        // 每 tick 重置 offset，只保留新的受伤部分
        data.hurtOffset = 0f;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        // 清空 offset
        ForceHurtUtil.UnhealableEntityData data = getData(entity);
        data.hurtOffset = 0f;
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }*/

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        ForceHurtUtil.makeNoHealable(entity);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        ForceHurtUtil.recoverFromNoHealable(entity);;
        
    }
}