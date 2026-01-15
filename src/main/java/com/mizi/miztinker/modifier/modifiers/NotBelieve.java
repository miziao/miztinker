package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class NotBelieve extends NoLevelsModifier implements DamageBlockModifierHook {

    public static final ModifierId ID = new ModifierId("miztinker", "not_believe");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
    }

    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry, EquipmentContext context,
                                   EquipmentSlot slot, DamageSource source, float damage) {
        if (!slot.getType().equals(EquipmentSlot.Type.ARMOR)) return false;
        return checkNotBelieveLogic(context.getEntity(), source);
    }

    public static boolean hasModifier(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    IToolStackView tool = ToolStack.from(stack);
                    if (tool.getModifierLevel(ID) > 0) return true;
                }
            }
        }
        return false;
    }

    public static boolean checkNotBelieveLogic(LivingEntity entity, DamageSource source) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO) || source.is(DamageTypeTags.BYPASSES_ARMOR)) return true;
        if (entity.hasEffect(MobEffects.BLINDNESS) || entity.hasEffect(MobEffects.DARKNESS)) return true;

        Entity attacker = source.getEntity();
        if (attacker != null) {
            Vec3 targetVec = attacker.position().subtract(entity.position()).normalize();
            Vec3 lookVec = entity.getViewVector(1.0F);
            return lookVec.dot(targetVec) < 0;
        }
        return false;
    }
}