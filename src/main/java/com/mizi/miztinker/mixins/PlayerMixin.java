package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.modifiers.EnchantedGold;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow
    protected FoodData foodData;

    @Unique
    private static final EquipmentSlot[] MIZTINKER$ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    @Unique
    private static final int MIZTINKER$MAX_FOOD_GAIN = 1;

    @Unique
    private static final float MIZTINKER$MAX_SATURATION_GAIN = 1.0F;

    @Unique
    private static final float MIZTINKER$EXHAUSTION_MULTIPLIER = 1.0F;

    @Unique
    private int miztinker$lastFoodLevel = 20;

    @Unique
    private float miztinker$lastSaturationLevel = 5.0F;

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void miztinker$canEatAlways(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        if (!ignoreHunger && EnchantedGold.hasEnchantedGold(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void miztinker$limitNoSuspicionStomachFoodGain(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (player.level().isClientSide) {
            return;
        }

        int currentFood = this.foodData.getFoodLevel();
        float currentSaturation = this.foodData.getSaturationLevel();

        if (!miztinker$hasNoSuspicionStomach(player)) {
            this.miztinker$lastFoodLevel = currentFood;
            this.miztinker$lastSaturationLevel = currentSaturation;
            return;
        }

        int foodGain = currentFood - this.miztinker$lastFoodLevel;

        if (foodGain > MIZTINKER$MAX_FOOD_GAIN) {
            int limitedFood = Math.min(20, this.miztinker$lastFoodLevel + MIZTINKER$MAX_FOOD_GAIN);
            this.foodData.setFoodLevel(limitedFood);
            currentFood = limitedFood;
        }

        float saturationGain = currentSaturation - this.miztinker$lastSaturationLevel;

        if (saturationGain > MIZTINKER$MAX_SATURATION_GAIN) {
            float limitedSaturation = this.miztinker$lastSaturationLevel + MIZTINKER$MAX_SATURATION_GAIN;
            limitedSaturation = Math.min(limitedSaturation, currentFood);
            this.foodData.setSaturation(limitedSaturation);
            currentSaturation = limitedSaturation;
        }

        if (currentSaturation > currentFood) {
            this.foodData.setSaturation(currentFood);
        }

        this.miztinker$lastFoodLevel = this.foodData.getFoodLevel();
        this.miztinker$lastSaturationLevel = this.foodData.getSaturationLevel();
    }

    @ModifyVariable(
            method = "causeFoodExhaustion(F)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float miztinker$doubleNoSuspicionStomachExhaustion(float exhaustion) {
        Player player = (Player) (Object) this;

        if (player.level().isClientSide) {
            return exhaustion;
        }

        if (!miztinker$hasNoSuspicionStomach(player)) {
            return exhaustion;
        }

        if (exhaustion <= 0.0F || Float.isNaN(exhaustion) || Float.isInfinite(exhaustion)) {
            return exhaustion;
        }

        return exhaustion * MIZTINKER$EXHAUSTION_MULTIPLIER;
    }

    @Unique
    private static boolean miztinker$hasNoSuspicionStomach(Player player) {
        for (EquipmentSlot slot : MIZTINKER$ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);

            if (miztinker$hasNoSuspicionStomachModifier(stack)) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private static boolean miztinker$hasNoSuspicionStomachModifier(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(TinkerTags.Items.MODIFIABLE)) {
            return false;
        }

        try {
            ToolStack tool = ToolStack.from(stack);

            return tool.getModifierLevel(
                    MiztinkerModifiers.No_suspicion_stomach.get()
            ) > 0;
        } catch (Exception ignored) {
            return false;
        }
    }
}