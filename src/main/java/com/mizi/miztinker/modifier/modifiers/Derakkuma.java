package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.MusicSlots;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

public class Derakkuma extends NoLevelsModifier implements VolatileDataModifierHook {
    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT nbt) {
        nbt.addSlots(MusicSlots.MUSIC,1);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.VOLATILE_DATA);
    }
}
