package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.ArrayList;
import java.util.List;

import static com.mizi.miztinker.miztinker.getResource;

public class DiamondOfLife extends NoLevelsModifier implements DamageBlockModifierHook, ToolStatsModifierHook, InventoryTickModifierHook {

    private static final net.minecraft.resources.ResourceLocation SHIELD_LOSS = getResource("diamond_life_loss");
    private static final net.minecraft.resources.ResourceLocation REGEN_TICK = getResource("diamond_life_regen_tick");

    private static final EquipmentSlot[] ARMOR_ORDER = new EquipmentSlot[] {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public boolean isDamageBlocked(
            IToolStackView tool,
            ModifierEntry modifier,
            EquipmentContext context,
            EquipmentSlot slotType,
            DamageSource source,
            float amount
    ) {
        LivingEntity entity = context.getEntity();

        if (entity.level().isClientSide
                || slotType.getType() != EquipmentSlot.Type.ARMOR
                || tool.isBroken()
                || amount <= 0.0f) {
            return false;
        }

        if (slotType != getFirstValidSlot(context)) {
            return false;
        }

        float simulatedDamage = simulateVanillaDamage(entity, source, amount);

        simulatedDamage = applyAllArmorModifyDamageHooks(
                context,
                source,
                simulatedDamage,
                true
        );

        simulatedDamage = sanitizeDamage(simulatedDamage);

        if (simulatedDamage <= 0.0f) {
            return true;
        }
        float totalAvailable = getTotalAvailableDiamondLifeArmor(context);

        if (totalAvailable < simulatedDamage) {
            return false;
        }

        consumeDiamondLifeArmor(context, simulatedDamage);

        return true;
    }

    private float simulateVanillaDamage(
            LivingEntity entity,
            DamageSource source,
            float amount
    ) {
        float damage = amount;

        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            float armorValue = entity.getArmorValue();
            float toughness = (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);

            damage = net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(
                    damage,
                    armorValue,
                    toughness
            );
        }

        var resistance = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);

        if (resistance != null) {
            damage *= Math.max(
                    1.0F - ((resistance.getAmplifier() + 1) * 0.2F),
                    0.0F
            );
        }

        return sanitizeDamage(damage);
    }

    private float applyAllArmorModifyDamageHooks(
            EquipmentContext context,
            DamageSource source,
            float amount,
            boolean isDirectDamage
    ) {
        float result = sanitizeDamage(amount);

        if (result <= 0.0f) {
            return 0.0f;
        }

        for (EquipmentSlot slot : ARMOR_ORDER) {
            IToolStackView armor = context.getToolInSlot(slot);

            if (armor == null || armor.isBroken()) {
                continue;
            }

            List<ModifierEntry> entries = new ArrayList<>(armor.getModifierList());

            for (ModifierEntry entry : entries) {
                if (entry == null) {
                    continue;
                } else {
                    entry.getModifier();
                }

                if (entry.getModifier() == this) {
                    continue;
                }

                if (!(entry.getModifier() instanceof ModifyDamageModifierHook hook)) {
                    continue;
                }

                result = hook.modifyDamageTaken(
                        armor,
                        entry,
                        context,
                        slot,
                        source,
                        result,
                        isDirectDamage
                );

                result = sanitizeDamage(result);

                if (result <= 0.0f) {
                    return 0.0f;
                }
            }
        }

        return result;
    }

    private float getTotalAvailableDiamondLifeArmor(EquipmentContext context) {
        float totalAvailable = 0.0f;

        for (EquipmentSlot slot : ARMOR_ORDER) {
            IToolStackView armor = context.getToolInSlot(slot);

            if (!isValidDiamondLifeArmor(armor)) {
                continue;
            }

            totalAvailable += armor.getStats().get(ToolStats.ARMOR);
        }

        return Math.max(totalAvailable, 0.0f);
    }

    private void consumeDiamondLifeArmor(EquipmentContext context, float damage) {
        float remaining = damage;

        for (EquipmentSlot slot : ARMOR_ORDER) {
            IToolStackView armor = context.getToolInSlot(slot);

            if (!isValidDiamondLifeArmor(armor)) {
                continue;
            }

            float available = armor.getStats().get(ToolStats.ARMOR);

            if (available <= 0.0f) {
                continue;
            }

            float consumed = Math.min(available, remaining);

            ModDataNBT armorData = armor.getPersistentData();
            float currentLoss = armorData.getFloat(SHIELD_LOSS);

            armorData.putFloat(SHIELD_LOSS, currentLoss + consumed);

            if (armor instanceof ToolStack ts) {
                ts.rebuildStats();
            }

            remaining -= consumed;

            if (remaining <= 0.0f) {
                break;
            }
        }
    }

    @Override
    public void addToolStats(
            IToolContext context,
            ModifierEntry modifier,
            ModifierStatsBuilder builder
    ) {
        float loss = context.getPersistentData().getFloat(SHIELD_LOSS);

        if (loss > 0.0f) {
            ToolStats.ARMOR.add(builder, -loss);
        }
    }

    @Override
    public void onInventoryTick(
            IToolStackView tool,
            ModifierEntry modifier,
            Level level,
            LivingEntity entity,
            int slotIndex,
            boolean isSelected,
            boolean isCorrectHold,
            ItemStack itemStack
    ) {
        if (level.isClientSide || tool.isBroken()) {
            return;
        }

        ModDataNBT data = tool.getPersistentData();

        float currentLoss = data.getFloat(SHIELD_LOSS);

        if (currentLoss > 0.0f) {
            int timer = data.getInt(REGEN_TICK) + 1;

            if (timer >= 20) {
                data.putFloat(SHIELD_LOSS, Math.max(currentLoss - 2.0f, 0.0f));
                data.putInt(REGEN_TICK, 0);

                if (tool instanceof ToolStack ts) {
                    ts.rebuildStats();
                }
            } else {
                data.putInt(REGEN_TICK, timer);
            }
        } else {
            data.putInt(REGEN_TICK, 0);
        }
    }

    private boolean isValidDiamondLifeArmor(IToolStackView armor) {
        return armor != null
                && !armor.isBroken()
                && armor.getModifierLevel(this) > 0
                && armor.getStats().get(ToolStats.ARMOR) > 0.0f;
    }

    private EquipmentSlot getFirstValidSlot(EquipmentContext context) {
        for (EquipmentSlot slot : ARMOR_ORDER) {
            IToolStackView stack = context.getToolInSlot(slot);

            if (isValidDiamondLifeArmor(stack)) {
                return slot;
            }
        }

        return null;
    }

    private float sanitizeDamage(float damage) {
        if (Float.isNaN(damage) || Float.isInfinite(damage)) {
            return 0.0f;
        }

        return Math.max(damage, 0.0f);
    }
}