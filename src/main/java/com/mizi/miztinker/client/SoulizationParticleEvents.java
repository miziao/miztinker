package com.mizi.miztinker.client;

import com.mizi.miztinker.modifier.modifiers.base.MizUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SoulizationParticleEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.isInvisible()) continue;

                if (livingEntity == mc.player && mc.options.getCameraType().isFirstPerson()) {
                    continue;
                }


                int level = MizUtil.getSoulizationLevel(livingEntity);
                if (level <= 0) continue;

                spawnSoulOrbitParticles(livingEntity);
            }
        }
    }

    private static void spawnSoulOrbitParticles(LivingEntity entity) {
        var world = entity.level();
        float time = (world.getGameTime() + Minecraft.getInstance().getFrameTime()) / 10.0F;

        int count = 6;
        float radius = 1.2F;

        for (int i = 0; i < count; i++) {
            double angleOffset = (i * Math.PI * 2.0 / count);
            double currentAngle = time + angleOffset;

            double x = entity.getX() + Math.cos(currentAngle) * radius;
            double z = entity.getZ() + Math.sin(currentAngle) * radius;
            double y = entity.getY() + (entity.getBbHeight() * 0.6) + Math.sin(time * 0.5) * 0.1;

            world.addParticle(ParticleTypes.SOUL, x, y, z, 0, 0.04, 0);

            if (world.random.nextFloat() < 0.5f) {
                world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.02, 0);
            }

            if (world.random.nextFloat() < 0.15f) {
                world.addParticle(ParticleTypes.SCULK_SOUL, x, y, z, 0, 0.07, 0);
            }
        }
    }
}