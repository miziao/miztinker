package com.mizi.miztinker.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;


@Mod.EventBusSubscriber(modid = "miztinker")
public class KillLootEventHandler {

    @SubscribeEvent
    public static void onLivingDrop(LivingDropsEvent event) {
        if (Math.random() < 0.0001) {

            Item soulFragment = ForgeRegistries.ITEMS.getValue(new ResourceLocation("miztinker", "soul_essence_fragments"));

            if (soulFragment != null) {
                ItemStack dropStack = new ItemStack(soulFragment);

                double x = event.getEntity().getX();
                double y = event.getEntity().getY();
                double z = event.getEntity().getZ();

                ItemEntity itemEntity = new ItemEntity(
                        event.getEntity().level(),
                        x, y, z,
                        dropStack
                );

                event.getDrops().add(itemEntity);
            }
        }
    }
}
