package com.mizi.miztinker.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "miztinker", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SmeltMapManager {
    private static final Map<Item, ItemStack> SMELT_CACHE = new HashMap<>();

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        SMELT_CACHE.clear();
    }

    public static void bakeRecipes(Level level) {
        if (!SMELT_CACHE.isEmpty()) return;

        for (SmeltingRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                try {
                    ItemStack[] items = ingredient.getItems();
                    for (ItemStack stack : items) {
                        if (stack != null && !stack.isEmpty()) {
                            SMELT_CACHE.putIfAbsent(stack.getItem(), recipe.getResultItem(level.registryAccess()));
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }

    public static ItemStack getResult(Item item) {
        ItemStack result = SMELT_CACHE.get(item);
        return result == null ? ItemStack.EMPTY : result.copy();
    }
}