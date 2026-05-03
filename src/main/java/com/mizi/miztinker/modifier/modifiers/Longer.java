package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;


import java.util.UUID;
import java.util.function.BiConsumer;

public class Longer extends Modifier implements AttributesModifierHook{

    private static final UUID REACH_UUID = UUID.fromString("a1b2c3d4-e5f6-4321-8765-1234567890ab");
    private static final UUID ATTACK_UUID = UUID.fromString("b2c3d4e5-f6a7-4321-8765-1234567890ac");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ATTRIBUTES);
    }

    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (slot == EquipmentSlot.MAINHAND) {
            int level = modifier.getLevel();
            double bonus = level * 1.8;

            consumer.accept(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(
                    REACH_UUID,
                    "Longer modifier reach",
                    bonus,
                    AttributeModifier.Operation.ADDITION
            ));

            consumer.accept(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(
                    ATTACK_UUID,
                    "Longer modifier attack",
                    bonus,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }
}