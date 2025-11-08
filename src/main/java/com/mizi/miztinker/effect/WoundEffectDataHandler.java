package com.mizi.miztinker.effect;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.mizi.miztinker.miztinker.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class WoundEffectDataHandler {
    public static final EntityDataAccessor<Float> WOUND_EFFECT_LAST_HEALTH =
            new EntityDataAccessor<>(100, EntityDataSerializers.FLOAT);
    @SubscribeEvent
    public static void onEntityConstruct(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof LivingEntity living) {

            living.getEntityData().define(WOUND_EFFECT_LAST_HEALTH, 0.0f);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {

            setLastHealth(living, living.getHealth());
        }
    }

    public static float getLastHealth(LivingEntity entity) {
        if (entity == null || entity.getEntityData() == null) {
            return 0.0f;
        }
        return entity.getEntityData().get(WOUND_EFFECT_LAST_HEALTH);
    }

    public static void setLastHealth(LivingEntity entity, float health) {
        if (entity != null && entity.getEntityData() != null) {
            entity.getEntityData().set(WOUND_EFFECT_LAST_HEALTH, health);
        }
    }
}