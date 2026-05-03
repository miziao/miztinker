package com.mizi.miztinker.modifier.modifiers;


import de.teamlapen.vampirism.api.VampirismAPI;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

public class BloodStomach extends NoLevelsModifier {

    public void convertFoodToBlood(Player player, ItemStack foodStack) {
        VampirismAPI.getVampirePlayer(player).ifPresent(vampire -> {
            if (vampire.getLevel() > 0 && foodStack.getItem().isEdible()) {
                FoodProperties food = foodStack.getItem().getFoodProperties(foodStack, player);
                if (food != null) {
                    int bloodAmount = Math.round(food.getNutrition() * (1.0f + food.getSaturationModifier()));

                    if (bloodAmount > 0) {
                        vampire.drinkBlood(bloodAmount, 1.0f, null);
                    }
                }
            }
        });
    }
}
