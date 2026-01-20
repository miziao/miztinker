package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.modifiers.EnchantedGold;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber
public class EnchantedGoldHandler {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @SubscribeEvent
    public static void onFoodEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!event.getItem().isEdible()) {
            return;
        }

        if (hasEnchantedGoldArmor(player)) {
            EnchantedGold.applyGoldEffects(player);
        }
    }

    private static boolean hasEnchantedGoldArmor(Player player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorStack = player.getItemBySlot(slot);
            if (checkTool(armorStack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkTool(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(slimeknights.tconstruct.common.TinkerTags.Items.MODIFIABLE)) {
            return false;
        }
        try {
            ToolStack tool = ToolStack.from(stack);
            return tool.getModifierLevel(MiztinkerModifiers.ENCHANTED_GOLD_STATIC_MODIFIER.get()) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}