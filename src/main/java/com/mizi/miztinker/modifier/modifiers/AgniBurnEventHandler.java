package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.mizi.miztinker.modifier.modifiers.Agni.TAG_AGNI_BURN;
import static com.mizi.miztinker.modifier.modifiers.Agni.clearAgniSelfBurn;
import static com.mizi.miztinker.modifier.modifiers.Agni.forceEntityBurn;
import static com.mizi.miztinker.modifier.modifiers.Agni.hasAgniEquipped;
import static com.mizi.miztinker.modifier.modifiers.Agni.isAgniBurning;
import static com.mizi.miztinker.modifier.modifiers.Agni.isAgniSelfBurning;
import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurt;

@Mod.EventBusSubscriber(modid = "miztinker")
public class AgniBurnEventHandler {

    private static final int BURN_DAMAGE_INTERVAL = 5;

    private static final float BURN_DAMAGE = 5.0f;

    private static final String TAG_BURN_DAMAGE_COOLDOWN = "agni_burn_damage_cooldown";

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        if (entity instanceof ServerPlayer player) {
            if (isAgniSelfBurning(player) && !hasAgniEquipped(player)) {
                clearAgniSelfBurn(player);
            }
        }

        if (!isAgniBurning(entity)) {
            return;
        }

        if (!entity.isAlive() || entity.isDeadOrDying()) {
            entity.getPersistentData().remove(TAG_AGNI_BURN);
            entity.getPersistentData().remove(TAG_BURN_DAMAGE_COOLDOWN);
            return;
        }

        forceEntityBurn(entity, 10);

        CompoundTag data = entity.getPersistentData();
        int cooldown = data.getInt(TAG_BURN_DAMAGE_COOLDOWN);

        if (cooldown <= 0) {
            DamageSource source = entity.damageSources().generic();

            forceHurt(entity, source, BURN_DAMAGE);

            data.putInt(TAG_BURN_DAMAGE_COOLDOWN, BURN_DAMAGE_INTERVAL);
        } else {
            data.putInt(TAG_BURN_DAMAGE_COOLDOWN, cooldown - 1);
        }
    }
}