package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import pyre.tinkerslevellingaddon.config.Config;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.build.RawDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;

import java.util.Random;


public class Double_Evolution_Pill extends NoLevelsModifier implements
        ValidateModifierHook,
        RawDataModifierHook,
        VolatileDataModifierHook {

    private static final ResourceLocation LEVEL_KEY = ResourceLocation.parse("tinkerslevellingaddon:level");
    private static final ResourceLocation APPLIED_FLAG = ResourceLocation.parse("miztinker:double_evolution_applied");
    private static final ResourceLocation KEY_SLOTS = ResourceLocation.parse("miztinker:double_evolution_slots");
    private static final ModifierId IMPROVABLE_ID = new ModifierId("tinkerslevellingaddon", "improvable");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.VALIDATE);
        hookBuilder.addHook(this, ModifierHooks.RAW_DATA);
        hookBuilder.addHook(this, ModifierHooks.VOLATILE_DATA);
    }

    @Override
    @Nullable
    public Component validate(IToolStackView tool, ModifierEntry modifier) {
        ModDataNBT data = tool.getPersistentData();
        if (data.getBoolean(APPLIED_FLAG)) return null;

        if (tool.getModifierLevel(IMPROVABLE_ID) <= 0) {
            return Component.translatable("recipe.miztinker.double_evolution.error_not_max_level");
        }

        int currentLevel = data.getInt(LEVEL_KEY);
        int maxConfigLevel = Config.maxLevel.get();

        if (currentLevel < maxConfigLevel) {
            return Component.translatable("recipe.miztinker.double_evolution.error_not_max_level", maxConfigLevel);
        }
        return null;
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
        ModDataNBT persistentData = (ModDataNBT) context.getPersistentData();

        if (persistentData.getBoolean(APPLIED_FLAG)) {
            CompoundTag slots = persistentData.getCompound(ResourceLocation.parse(KEY_SLOTS.toString()));
            for (String key : slots.getAllKeys()) {
                SlotType slotType = SlotType.getIfPresent(key);
                if (slotType != null) {
                    volatileData.addSlots(slotType, slots.getInt(key));
                }
            }
        }
    }

    @Override
    public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag restrictedData) {
        ModDataNBT persistentData = tool.getPersistentData();
        if (persistentData.getBoolean(APPLIED_FLAG)) return;


        if (validate(tool, modifier) != null) return;

        CompoundTag slotsTag = new CompoundTag();
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            String slotName = getRandomSlot(rand).getName();
            slotsTag.putInt(slotName, slotsTag.getInt(slotName) + 1);
        }

        persistentData.put(KEY_SLOTS, slotsTag);
        persistentData.putBoolean(APPLIED_FLAG, true);
    }

    @Override
    public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag restrictedData) {
        ModDataNBT persistentData = tool.getPersistentData();
        persistentData.remove(APPLIED_FLAG);
        persistentData.remove(KEY_SLOTS);
    }

    private SlotType getRandomSlot(Random rand) {
        int r = rand.nextInt(4);
        return switch (r) {
            case 0 -> SlotType.UPGRADE;
            case 1 -> SlotType.ABILITY;
            case 2 -> SlotType.DEFENSE;
            default -> SlotType.SOUL;
        };
    }
}