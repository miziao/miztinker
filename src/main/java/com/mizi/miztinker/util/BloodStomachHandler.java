package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber
public class BloodStomachHandler {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @SubscribeEvent
    public static void onVampireEat(LivingEntityUseItemEvent.Finish event) {
        if (!ModList.get().isLoaded("vampirism") || MiztinkerModifiers.BLOOD_STOMACH_STATIC_MODIFIER == null) {
            return;
        }

        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack itemStack = event.getItem();
        if (!itemStack.isEdible()) return;

        int level = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.is(slimeknights.tconstruct.common.TinkerTags.Items.MODIFIABLE)) {
                try {
                    ToolStack tool = ToolStack.from(stack);
                    if (!tool.isBroken()) {
                        level = Math.max(level, tool.getModifierLevel(MiztinkerModifiers.BLOOD_STOMACH_STATIC_MODIFIER.get()));
                    }
                } catch (Exception ignored) {}
            }
        }

        if (level > 0) {
            MiztinkerModifiers.BLOOD_STOMACH_STATIC_MODIFIER.get().convertFoodToBlood(player, itemStack);
        }
    }
}