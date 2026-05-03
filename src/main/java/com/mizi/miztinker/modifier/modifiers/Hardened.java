package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.technical.ArmorLevelModule;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Hardened extends Modifier implements ModifyDamageModifierHook {
    public static final TinkerDataCapability.TinkerDataKey<Integer> KEY_HARDENED =
            TinkerDataCapability.TinkerDataKey.of(ResourceLocation.fromNamespaceAndPath("miztinker", "hardened"));

    public static final TinkerDataCapability.TinkerDataKey<Float> KEY_ORIGINAL_RAW_DAMAGE =
            TinkerDataCapability.TinkerDataKey.of(ResourceLocation.fromNamespaceAndPath("miztinker", "original_raw_damage"));

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addModule(new ArmorLevelModule(KEY_HARDENED, false, TinkerTags.Items.ARMOR));
        hookBuilder.addHook(this, ModifierHooks.MODIFY_HURT);
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry entry, EquipmentContext context, EquipmentSlot slot, DamageSource source, float amount, boolean isDirectDamage) {
        if (amount <= 0) return amount;

        if (slot != getFirstValidSlot(context)) {
            return amount;
        }


        return context.getTinkerData().map(data -> {
            if (!data.contains(KEY_ORIGINAL_RAW_DAMAGE)) {
                data.put(KEY_ORIGINAL_RAW_DAMAGE, amount);
                return amount;
            }

            float original = data.get(KEY_ORIGINAL_RAW_DAMAGE, amount);

            data.remove(KEY_ORIGINAL_RAW_DAMAGE);

            if (original <= amount) return amount;

            int totalLevel = data.get(KEY_HARDENED, 0);
            if (totalLevel <= 0) return amount;

            float currentRatio = amount / original;

            int repeatTimes = totalLevel * 5;

            double finalMultiplier = Math.pow(currentRatio, repeatTimes);
            float finalAmount = (float) (amount * finalMultiplier);

            return Math.max(0, finalAmount);
        }).orElse(amount);
    }

    private EquipmentSlot getFirstValidSlot(EquipmentContext context) {
        for (EquipmentSlot s : EquipmentSlot.values()) {
            IToolStackView stack = context.getToolInSlot(s);
            if (stack != null && !stack.isBroken() && stack.getModifierLevel(this) > 0) {
                return s;
            }
        }
        return null;
    }
}