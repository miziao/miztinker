package com.mizi.miztinker.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import static com.mizi.miztinker.modifier.modifiers.base.AbsoluteSeverance.*;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.*;


public class DestinedDeath extends MobEffect {
    public DestinedDeath() {
        super(MobEffectCategory.HARMFUL, 16769263);
        super.addAttributeModifier(Attributes.MAX_HEALTH, "8790CFC6-C6A2-522E-F429-C219017E870A", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
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
        modifierAbsoluteSeverance(living);
    }
    public static void modifierAbsoluteSeverance(LivingEntity target){
        if (target.getHealth() <= 0) return;
        float reHealth = target.getHealth() - target.getMaxHealth() * 0.001f;
        setAbsoluteSeveranceHealth(target, reHealth);
        forceSetAllCandidateHealth(target,reHealth);

        if (reHealth <= 0 || target.getHealth() <= 0){
            forceSetAllCandidateHealth(target, 0);
            setAbsoluteSeveranceHealth(target, 0);
            setEntityDead(target);
        }
    }
}
