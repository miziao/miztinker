package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.EmbossmentModifierHook;
import com.mizi.miztinker.modifier.modifiers.base.MiztinkerHooks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class Soulization extends Modifier implements EmbossmentModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, MiztinkerHooks.EMBOSSMENT);
    }

    @Override
    public boolean applyItem(EmbossmentContext context, int inputIndex, boolean secondary) {
        ItemStack input = context.getInputStack(inputIndex);

        if (input.hasTag() && input.getTag() != null && input.getTag().contains("embossed")) {
            CompoundTag embossedTag = input.getTag().getCompound("embossed");

            if (embossedTag.contains("tag")) {
                ToolStack targetTool = ToolStack.from(context.getToolStack());

                ItemStack virtualStack = new ItemStack(input.getItem());
                virtualStack.setTag(embossedTag.getCompound("tag"));
                ToolStack sourceTool = ToolStack.from(virtualStack);

                for (ModifierEntry entry : sourceTool.getModifierList()) {
                    targetTool.addModifier(entry.getModifier().getId(), entry.getLevel());
                }

                return true;
            }
        }
        return false;
    }
}