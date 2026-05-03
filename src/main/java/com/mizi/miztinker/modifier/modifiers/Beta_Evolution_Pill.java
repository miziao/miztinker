package com.mizi.miztinker.modifier.modifiers;

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
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;



public class Beta_Evolution_Pill extends Modifier implements
        RawDataModifierHook,
        ValidateModifierHook {

    private static final ResourceLocation LEVEL_KEY = ResourceLocation.parse("tinkerslevellingaddon:level");
    private static final ResourceLocation EXP_KEY = ResourceLocation.parse("tinkerslevellingaddon:experience");
    private static final ResourceLocation EVOLUTION_COUNT = ResourceLocation.parse("mizi:beta_evolution_count");
    private static final ModifierId IMPROVABLE_ID = new ModifierId("tinkerslevellingaddon", "improvable");
    private static final ResourceLocation RECURSION_LOCK = ResourceLocation.parse("miztinker:is_evolving");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.VALIDATE);
        hookBuilder.addHook(this, ModifierHooks.RAW_DATA);
    }

    @Override
    @Nullable
    public Component validate(IToolStackView tool, ModifierEntry modifier) {
        ModDataNBT data = tool.getPersistentData();
        int alreadyEvolved = data.getInt(EVOLUTION_COUNT);


        if (modifier.getLevel() <= alreadyEvolved) {
            return null;
        }

        if (tool.getModifierLevel(IMPROVABLE_ID) <= 0) {
            return Component.translatable("recipe.miztinker.beta_evolution.error_level_low");
        }

        int currentLevel = data.getInt(LEVEL_KEY);
        int maxConfigLevel = Config.maxLevel.get();
        if (currentLevel < maxConfigLevel) {
            return Component.translatable("recipe.miztinker.beta_evolution.error_level_low", maxConfigLevel);
        }
        return null;
    }

    @Override
    public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag restrictedTag) {
        ModDataNBT persistentData = tool.getPersistentData();

        int alreadyEvolved = persistentData.getInt(EVOLUTION_COUNT);


        if (modifier.getLevel() <= alreadyEvolved) return;
        if (persistentData.getBoolean(RECURSION_LOCK)) return;
        if (validate(tool, modifier) != null) return;

        if (tool instanceof ToolStack toolStack) {
            persistentData.putBoolean(RECURSION_LOCK, true);
            try {
                ModifierNBT allMods = tool.getModifiers();

                for (ModifierEntry entry : allMods.getModifiers()) {
                    if (entry.getModifier() == this) continue;

                    toolStack.addModifier(entry.getId(), 1);
                }

                resetToolProgress(tool);

                persistentData.putInt(EVOLUTION_COUNT, modifier.getLevel());

            } finally {
                persistentData.remove(RECURSION_LOCK);
            }
        }
    }

    @Override
    public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag restrictedTag) {
        tool.getPersistentData().remove(EVOLUTION_COUNT);
    }

    private void resetToolProgress(IToolStackView tool) {
        ModDataNBT data = tool.getPersistentData();
        data.putInt(LEVEL_KEY, 0);
        data.putInt(EXP_KEY, 0);
    }
}