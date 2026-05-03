package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import static pyre.tinkerslevellingaddon.ImprovableModifier.EXPERIENCE_KEY;

public class Invert extends Modifier implements ValidateModifierHook {

    private static final ResourceLocation LAST_LEVEL_KEY =
            ResourceLocation.fromNamespaceAndPath("miztinker", "invert_last_level");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.VALIDATE);
    }

    @Override
    public Component validate(IToolStackView tool, ModifierEntry modifier) {
        ModDataNBT data = tool.getPersistentData();

        int currentLevel = modifier.getLevel();
        int currentXp = data.getInt(EXPERIENCE_KEY);

        if (currentXp == 0) {
            return null;
        }

        int lastLevel = data.getInt(LAST_LEVEL_KEY);

        if (currentLevel > lastLevel) {
            int invertedXp = -currentXp;
            data.putInt(EXPERIENCE_KEY, invertedXp);

            data.putInt(LAST_LEVEL_KEY, currentLevel);
        }

        return null;
    }
}