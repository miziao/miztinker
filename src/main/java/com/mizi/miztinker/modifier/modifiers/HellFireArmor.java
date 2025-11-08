package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class HellFireArmor extends NoLevelsModifier implements ModifyDamageModifierHook {

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry entry, EquipmentContext context,
                                   EquipmentSlot slot, DamageSource source, float amount, boolean isDirectDamage) {

        LivingEntity entity = context.getEntity();
        if (entity != null && entity.level().dimension().location().equals(new ResourceLocation("minecraft:the_nether"))) {
            // 在下界时，受到的伤害降低 20%
            return amount * 0.8F;
        }
        return amount;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MODIFY_DAMAGE);
    }
}
