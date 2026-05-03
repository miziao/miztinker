package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MizInputEventHandler {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        LeftClickModifierHook.handleLeftClick(event.getItemStack(), event.getEntity(), EquipmentSlot.MAINHAND);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        LeftClickModifierHook.handleLeftClickBlock(
                event.getItemStack(),
                event.getEntity(),
                EquipmentSlot.MAINHAND,
                event.getLevel().getBlockState(event.getPos()),
                event.getPos()
        );
    }
}