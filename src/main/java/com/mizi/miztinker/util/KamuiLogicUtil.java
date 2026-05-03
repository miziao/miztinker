package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.mizi.miztinker.miztinker.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KamuiLogicUtil {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKamuiTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide()) return;

        if (entity.hasEffect(MiztinkerEffect.KAMUI_PLUS.get())) {
            ForceHurtUtil.makeHealthIncreasingOnly(entity);

            entity.invulnerableTime = 20;

            if (entity instanceof Player player) {
                if (player.deathTime > 0) {
                    player.deathTime = 0;
                    player.deathScore = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        if (event.getEffect() == MiztinkerEffect.KAMUI_PLUS.get()) {
            boolean isBypassed = com.mizi.miztinker.effect.Pair_Kamui_effect.BYPASS_THREAD_LOCAL.get();
            MobEffectInstance inst = event.getEffectInstance();
            boolean isEnding = (inst != null && inst.getDuration() <= 1);

            if (isBypassed || isEnding) {
                if (!entity.level().isClientSide()) {
                    ForceHurtUtil.recoverToNormalHealth(entity);
                }
                return;
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKamuiDeath(LivingDeathEvent event) {
        if (event.getEntity().hasEffect(MiztinkerEffect.KAMUI_PLUS.get())) {
            event.setCanceled(true);
            event.getEntity().setHealth(event.getEntity().getMaxHealth());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKamuiHurt(LivingHurtEvent event) {
        if (event.getEntity().hasEffect(MiztinkerEffect.KAMUI_PLUS.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect() == MiztinkerEffect.KAMUI_PLUS.get()) {
            ForceHurtUtil.recoverToNormalHealth(event.getEntity());
        }
    }

}