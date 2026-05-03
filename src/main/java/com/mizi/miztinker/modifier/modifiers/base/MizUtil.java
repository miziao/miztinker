package com.mizi.miztinker.modifier.modifiers.base;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

public class MizUtil {

    public static boolean hasAll_Perfect(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && ModifierUtil.getModifierLevel(stack, MiztinkerModifiers.ALL_PERFECT_STATIC_MODIFIER.getId()) > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAE86(LivingEntity entity) {
        if (entity == null) return false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && ModifierUtil.getModifierLevel(stack, MiztinkerModifiers.AE86.getId()) > 0) {
                return true;
            }
        }
        return false;
    }

    public static int getSoulizationLevel(LivingEntity entity) {
        if (entity == null) return 0;
        int totalLevel = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                totalLevel += ModifierUtil.getModifierLevel(stack, MiztinkerModifiers.SOULIZATION_ARMOR_STATIC_MODIFIER.getId());
            }
        }
        return totalLevel;
    }
}