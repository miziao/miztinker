package com.mizi.miztinker.modifier.modifiers;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;


public class OldWritable extends Modifier implements VolatileDataModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.VOLATILE_DATA);
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT nbt) {
        int level = modifier.getLevel();

        if (level > 0) {
            nbt.addSlots(SlotType.UPGRADE, level);
            nbt.addSlots(SlotType.ABILITY, level);
            nbt.addSlots(SlotType.DEFENSE, level);
            nbt.addSlots(SlotType.SOUL, level); 
        }
    }
}