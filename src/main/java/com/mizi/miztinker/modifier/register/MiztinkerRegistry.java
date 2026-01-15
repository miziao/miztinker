package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.recipes.SoulizationRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

public class MiztinkerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "miztinker");

    public static final RegistryObject<RecipeSerializer<SoulizationRecipe>> SOUL_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("soulization_recipe",
                    () -> LoadableRecipeSerializer.of(SoulizationRecipe.LOADER));
}