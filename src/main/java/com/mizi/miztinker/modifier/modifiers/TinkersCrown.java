package com.mizi.miztinker.modifier.modifiers;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Objects;
import java.util.UUID;

public class TinkersCrown extends NoLevelsModifier implements EquipmentChangeModifierHook {

    private static final UUID MAX_MANA_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SPELL_POWER_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID COOLDOWN_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID CAST_TIME_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, slimeknights.tconstruct.library.modifiers.ModifierHooks.EQUIPMENT_CHANGE);
    }

    @Override
    public void onEquip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof Player player)) return;

        // MAX_MANA +10000
        addAttribute(player, AttributeRegistry.MAX_MANA.get(), MAX_MANA_UUID, 10000);

        // SPELL_POWER +200%
        addAttribute(player, AttributeRegistry.SPELL_POWER.get(), SPELL_POWER_UUID, 2.0); // 乘 2 即 +200%

        // COOLDOWN_REDUCTION +0.75 (75%)
        addAttribute(player, AttributeRegistry.COOLDOWN_REDUCTION.get(), COOLDOWN_UUID, 0.75);

        // CAST_TIME_REDUCTION -0.5 (降低施法速度)
        addAttribute(player, AttributeRegistry.CAST_TIME_REDUCTION.get(), CAST_TIME_UUID, -0.5);
    }

    @Override
    public void onUnequip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof Player player)) return;

        // 移除加成
        removeAttribute(player, AttributeRegistry.MAX_MANA.get(), MAX_MANA_UUID);
        removeAttribute(player, AttributeRegistry.SPELL_POWER.get(), SPELL_POWER_UUID);
        removeAttribute(player, AttributeRegistry.COOLDOWN_REDUCTION.get(), COOLDOWN_UUID);
        removeAttribute(player, AttributeRegistry.CAST_TIME_REDUCTION.get(), CAST_TIME_UUID);
    }

    private void addAttribute(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id, double value) {
        if (Objects.requireNonNull(player.getAttribute(attribute)).getModifier(id) == null) {
            Objects.requireNonNull(player.getAttribute(attribute)).addTransientModifier(
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, "TinkersCrownModifier", value, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION)
            );
        }
    }

    private void removeAttribute(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id) {
        if (Objects.requireNonNull(player.getAttribute(attribute)).getModifier(id) != null) {
            Objects.requireNonNull(player.getAttribute(attribute)).removeModifier(id);
        }
    }
}
