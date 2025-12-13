package com.mizi.miztinker.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.reflectionPenetratingDamage;


public class DestinedDeath extends MobEffect {
    public DestinedDeath() {
        super(MobEffectCategory.HARMFUL, 16769263);
        super.addAttributeModifier(Attributes.MAX_HEALTH, "F5D8D627-2788-AF4C-2DA5-FBA1650E264D", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    public @NotNull String getDescriptionId () {
        return "effect.miztinker.destineddeath";
    }
    public boolean isDurationEffectTick (int duration, int amplifier) {
        return true;
    }
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap map, int level) {
        super.addAttributeModifiers(entity, map, level);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
    @Override
    public void applyEffectTick(LivingEntity living, int amplifier) {
        reflectionPenetratingDamage(living, living, living.getMaxHealth() * 0.002f + 1);
    }
}
