package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

public class HadStomach extends NoLevelsModifier {

    public void healByFood(Player player, ItemStack foodStack) {
        if (foodStack.getItem().isEdible()) {
            FoodProperties food = foodStack.getItem().getFoodProperties(foodStack, player);
            if (food != null) {
                int nutrition = food.getNutrition();
                float saturationMod = food.getSaturationModifier();

                float healAmount = nutrition * (1.0f + saturationMod);

                if (healAmount > 0) {
                    player.heal(healAmount);
                }
            }
        }
    }
}