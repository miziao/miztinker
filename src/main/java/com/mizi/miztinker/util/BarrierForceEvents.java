package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.modifiers.BarrierForce;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BarrierForceEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() != null && BarrierForce.hasBarrierForce(event.getNewTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (BarrierForce.hasBarrierForce(victim)) {
            event.setCanceled(true);
        }
    }

    @Mod.EventBusSubscriber(modid = "miztinker", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
            if (BarrierForce.hasBarrierForce(event.getEntity())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderHand(RenderHandEvent event) {
            Player player = Minecraft.getInstance().player;
            if (BarrierForce.hasBarrierForce(player)) {
                event.setCanceled(true);
            }
        }
    }
}