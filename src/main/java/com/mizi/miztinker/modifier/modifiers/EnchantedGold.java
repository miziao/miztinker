package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

public class EnchantedGold extends NoLevelsModifier {
    public static final int MAX_ABSORB_TIME = 2400;
    public static final int MAX_HEAL_TIME = 400;
    public static final int MAX_FIRE_TIME = 6000;
    public static final int MAX_RESISTANCE_TIME = 6000;

    public static void applyGoldEffects(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, MAX_ABSORB_TIME, 3, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, MAX_HEAL_TIME, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MAX_FIRE_TIME, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, MAX_RESISTANCE_TIME, 0, false, false, true));
    }

    public static boolean hasEnchantedGold(Player player) {
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (slot.isArmor()) {
                slimeknights.tconstruct.library.tools.nbt.IToolStackView tool = slimeknights.tconstruct.library.tools.nbt.ToolStack.from(player.getItemBySlot(slot));
                if (!tool.isBroken() && tool.getModifierLevel(MiztinkerModifiers.ENCHANTED_GOLD_STATIC_MODIFIER.get()) > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}