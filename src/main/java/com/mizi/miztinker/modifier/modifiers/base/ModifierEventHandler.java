package com.mizi.miztinker.modifier.modifiers.base;

import com.mizi.miztinker.miztinker;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.modifiers.Modifier;

import static com.mizi.miztinker.modifier.register.MiztinkerModifiers.ABYSS_MAW_STATIC_MODIFIER;

@Mod.EventBusSubscriber(modid = miztinker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModifierEventHandler {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        UseAnim anim = event.getItem().getUseAnimation();
        if (anim == UseAnim.EAT || anim == UseAnim.DRINK) {

            Modifier targetModifier = ABYSS_MAW_STATIC_MODIFIER.get();
            boolean hasModifier = false;

            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack armorStack = entity.getItemBySlot(slot);

                if (!armorStack.isEmpty() && ModifierUtil.getModifierLevel(armorStack, targetModifier.getId()) > 0) {
                    hasModifier = true;
                    break;
                }
            }

            if (hasModifier) {
                event.setDuration(5);
            }
        }
    }
}