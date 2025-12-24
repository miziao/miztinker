package com.mizi.miztinker.modifier.modifiers;


import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;

public class Old_Spiky extends Modifier implements OnAttackedModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        // 注册受击 Hook
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    @Override
    public void onAttacked(
            IToolStackView tool,
            ModifierEntry modifier,
            EquipmentContext context,
            EquipmentSlot slotType,
            DamageSource source,
            float amount,
            boolean isDirectDamage
    ) {
        LivingEntity victim = context.getEntity();

        if (victim.level().isClientSide) return;
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

        if (source.is(DamageTypes.THORNS) || source.is(DamageTypes.CACTUS)) return;

        if (slotType != EquipmentSlot.CHEST &&
                slotType != EquipmentSlot.MAINHAND &&
                slotType != EquipmentSlot.OFFHAND) return;

        int totalLevel = 0;
        float baseDamage = 0f;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            IToolStackView stack = context.getToolInSlot(slot);
            if (stack == null || stack.isBroken()) continue;

            int level = stack.getModifierLevel(this);
            if (level > 0) {
                totalLevel += level;

                float dmg = stack.getStats().get(ToolStats.ATTACK_DAMAGE);
                if (dmg > 0) {
                    baseDamage += dmg;
                }
            }
        }


        if (slotType.getType() == EquipmentSlot.Type.ARMOR) {
            EquipmentSlot[] hands = {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
            for (EquipmentSlot hand : hands) {
                IToolStackView handTool = context.getToolInSlot(hand);
                if (handTool != null && handTool.getModifierLevel(this) == 0) {
                    baseDamage += handTool.getStats().get(ToolStats.ATTACK_DAMAGE);
                }
            }
        }

        if (totalLevel <= 0) return;

        if (baseDamage <= 1.0F) {
            baseDamage = 2.0F;
        }

        float multiplier = totalLevel * 0.5f;

        if (victim.isBlocking()) {
            multiplier *= 1.5f;
        }

        float finalDamage = baseDamage * multiplier;

        if (finalDamage > 0) {
            DamageSource thorns = victim.damageSources().thorns(victim);
            attacker.hurt(thorns, finalDamage);
        }
    }
}