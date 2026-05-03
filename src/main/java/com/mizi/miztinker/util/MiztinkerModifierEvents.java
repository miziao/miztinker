package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class MiztinkerModifierEvents {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        LeftClickModifierHook.handleLeftClickBlock(
                event.getItemStack(),
                event.getEntity(),
                event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND,
                event.getLevel().getBlockState(event.getPos()),
                event.getPos()
        );
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        LeftClickModifierHook.handleLeftClick(
                event.getItemStack(),
                event.getEntity(),
                event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND
        );
    }
}