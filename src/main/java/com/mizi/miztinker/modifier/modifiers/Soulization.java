package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.EmbossmentModifierHook;
import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import java.util.List;

public class Soulization extends NoLevelsModifier implements EmbossmentModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, MiztinkerHooks.EMBOSSMENT);
    }

    @Override
    public boolean applyItem(EmbossmentContext context, int inputIndex, boolean secondary) {
        ItemStack input = context.getInputStack(inputIndex);

        if (input.getItem() instanceof IMaterialItem materialItem) {
            MaterialVariantId materialVariant = materialItem.getMaterial(input);

            if (materialVariant.getId().equals(slimeknights.tconstruct.library.materials.definition.IMaterial.UNKNOWN_ID)) {
                context.setErrorMsg(Component.translatable("recipe.miztinker.unknown_material"));
                return false;
            }

            ToolStack targetTool = ToolStack.from(context.getToolStack());

            List<ModifierEntry> traits;
            if (input.getItem() instanceof IToolPart toolPart) {
                traits = MaterialRegistry.getInstance().getTraits(materialVariant.getId(), toolPart.getStatType());
            } else {
                traits = MaterialRegistry.getInstance().getTraits(materialVariant.getId(), new MaterialStatsId("tconstruct", "empty"));
            }

            if (traits.isEmpty()) {
                context.setErrorMsg(Component.translatable("recipe.miztinker.no_traits_found"));
                return false;
            }

            for (ModifierEntry trait : traits) {
                targetTool.addModifier(trait.getId(), trait.getLevel());
            }

            return true;
        } else {
            context.setErrorMsg(Component.translatable("recipe.miztinker.not_a_tinker_part"));
            return false;
        }
    }
}