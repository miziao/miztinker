package com.mizi.miztinker.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "miztinker")
public class KillLootEventHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide) return;

        if (RANDOM.nextFloat() < 0.05F) {
            spawnCustomItem(entity, "miztinker:soul_essence_fragments", 1);
        }

        if (entity instanceof WitherBoss) {
            int count = RANDOM.nextInt(16) + 15;
            spawnCustomItem(entity, "miztinker:hallowed_bar", count);
        }

        if (entity instanceof ServerPlayer) {
            if (RANDOM.nextFloat() < 0.2F) {
                spawnCustomItem(entity, "miztinker:soul_essence_fragments", 1);
            }
        }
    }

    private static void spawnCustomItem(LivingEntity entity, String registryName, int count) {
        ResourceLocation loc = ResourceLocation.tryParse(registryName);
        if (loc != null) {
            Item item = ForgeRegistries.ITEMS.getValue(loc);
            if (item != null) {
                entity.spawnAtLocation(new ItemStack(item, count));
            }
        }
    }
}