package com.mizi.miztinker.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 创伤效果：存在期间禁止生命恢复。
 */

public class WoundEffect extends MobEffect {

    public WoundEffect() {
        super(MobEffectCategory.HARMFUL, 0x555555); // 设置禁疗效果颜色
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // 每 tick 执行一次
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 获取当前生命值和存储的上次生命值
        float current = entity.getHealth();
        // 如果 key 不存在，则初始化为当前生命值
        if (!entity.getPersistentData().contains("wound_effect_last_health")) {
            entity.getPersistentData().putFloat("wound_effect_last_health", current);
            return; // 第一次 tick 不处理
        }
        float last = entity.getPersistentData().getFloat("wound_effect_last_health");

        // 禁止回血
        if (current > last) {
            entity.setHealth(last);
        }

        // 更新 last
        entity.getPersistentData().putFloat("wound_effect_last_health", entity.getHealth());
    }
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        // 移除时清空数据
        entity.getPersistentData().remove("wound_effect_last_health");
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}