package com.mizi.miztinker.modifier.hook;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.Collection;

public interface ArrowDamageModifierHook {
    float getArrowDamage(ModDataNBT persistentData, ModifierEntry entry, ModifierNBT modifiers, AbstractArrow arrow, @Nullable LivingEntity attacker, @NotNull Entity target, float baseDamage, float damage);

    class DefaultClass implements ArrowDamageModifierHook {
        @Override
        public float getArrowDamage(ModDataNBT persistentData, ModifierEntry entry, ModifierNBT modifiers, AbstractArrow arrow, @Nullable LivingEntity attacker, @NotNull Entity target, float baseDamage, float damage) {
            return damage;
        }
    }

    record AllMerger(Collection<ArrowDamageModifierHook> modules) implements ArrowDamageModifierHook {
        @Override
        public float getArrowDamage(ModDataNBT persistentData, ModifierEntry entry, ModifierNBT modifiers, AbstractArrow arrow, @Nullable LivingEntity attacker, @NotNull Entity target, float baseDamage, float damage) {
            for (ArrowDamageModifierHook module : modules) {
                damage = module.getArrowDamage(persistentData, entry, modifiers, arrow, attacker, target, baseDamage, damage);
            }
            return damage;
        }
    }
}