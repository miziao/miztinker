package com.mizi.miztinker.modifier.modifiers.base;


import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.world.entity.EquipmentSlot;
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
}
