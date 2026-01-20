package com.mizi.miztinker.recipes;

import com.mizi.miztinker.modifier.modifiers.base.EmbossmentModifierHook;
import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import com.mizi.miztinker.modifier.register.MiztinkerRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.adding.AbstractModifierRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.IMutableTinkerStationContainer;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationContainer;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoulizationRecipe extends AbstractModifierRecipe {

    public static final RecordLoadable<SoulizationRecipe> LOADER = RecordLoadable.create(
            ContextKey.ID.requiredField(),
            SizedIngredient.LOADABLE.list(1).requiredField("inputs", r -> r.inputs),
            SizedIngredient.LOADABLE.list(1).requiredField("soul_inputs", r -> r.soulItem),
            TOOLS_FIELD,
            MAX_TOOL_SIZE_FIELD,
            RESULT_FIELD,
            LEVEL_FIELD,
            SLOTS_FIELD,
            SoulizationRecipe::new
    );

    private final List<SizedIngredient> soulItem;
    private final List<SizedIngredient> inputs;
    private List<SizedIngredient> ingredientsCache;

    public SoulizationRecipe(ResourceLocation id, List<SizedIngredient> inputs, List<SizedIngredient> soulItem, Ingredient toolRequirement, int maxToolSize, ModifierId result, IntRange level, @Nullable SlotCount slots) {
        super(id, toolRequirement, maxToolSize, result, level, slots, false, false);
        this.inputs = inputs;
        this.soulItem = soulItem;
    }

    @Override
    public boolean matches(ITinkerStationContainer container, Level level) {
        if (!result.isBound() || !this.toolRequirement.test(container.getTinkerableStack())) {
            return false;
        }
        if (ingredientsCache == null) {
            ingredientsCache = new ArrayList<>();
        } else {
            ingredientsCache.clear();
        }
        ingredientsCache.addAll(inputs);
        ingredientsCache.addAll(soulItem);
        return ModifierRecipe.checkMatch(container, ingredientsCache);
    }

    @Override
    public RecipeResult<LazyToolStack> getValidatedResult(ITinkerStationContainer inv, RegistryAccess access) {
        ToolStack tool = inv.getTinkerable();
        var commonError = this.validatePrerequisites(tool);
        if (commonError != null) {
            return RecipeResult.failure(commonError);
        }

        ModifierId modifierId = result.getId();
        tool = tool.copy();

        if (tool.getModifierLevel(modifierId) == 0) {
            SlotCount slots = getSlots();
            if (slots != null) {
                tool.getPersistentData().addSlots(slots.type(), -slots.count());
            }
        } else {
            tool.removeModifier(modifierId, 1);
        }

        tool.addModifier(modifierId, 1);
        boolean success = false;
        ItemStack resultStack = tool.createStack();

        EmbossmentModifierHook.EmbossmentContext context = new EmbossmentModifierHook.EmbossmentContext(resultStack, inv);

        boolean secondary = false;
        for (int i = 0; i < inv.getInputCount(); i++) {
            ItemStack inputStack = inv.getInput(i);
            if (!soulItem.isEmpty() && soulItem.get(0).test(inputStack)) {
                success = tool.getModifier(modifierId)
                        .getHook(MiztinkerHooks.EMBOSSMENT)
                        .applyItem(context, i, secondary);
            }
            secondary = true;
        }

        if (success) {
            return LazyToolStack.success(context.getToolStack());
        }
        return RecipeResult.failure(context.getErrorMsg());
    }

    @Override
    public void updateInputs(LazyToolStack result, IMutableTinkerStationContainer inv, boolean isServer) {
        ModifierRecipe.updateInputs(inv, inputs);
        ModifierRecipe.updateInputs(inv, soulItem);
    }

    @Override
    public int getInputCount() {
        return inputs.size() + soulItem.size();
    }

    @Override
    public List<ItemStack> getDisplayItems(int slot) {
        if (slot >= 0 && slot < inputs.size() + soulItem.size()) {
            if (slot < inputs.size()) {
                return inputs.get(slot).getMatchingStacks();
            } else {
                return soulItem.get(slot - inputs.size()).getMatchingStacks();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MiztinkerRegistry.SOUL_RECIPE_SERIALIZER.get();
    }
}