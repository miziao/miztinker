package com.mizi.miztinker.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "miztinker")
public class WitchLootEventHandler {

    @SubscribeEvent
    public static void onWitchDrop(LivingDropsEvent event) {
        if (event.getEntity() instanceof Witch) {

            if (event.getSource().is(DamageTypes.IN_FIRE) ||
                    event.getSource().is(DamageTypes.ON_FIRE) ||
                    event.getSource().is(DamageTypes.LAVA) ||
                    event.getSource().is(DamageTypes.HOT_FLOOR)) {

                if (Math.random() < 1.0) {
                    Item witchFiber = ForgeRegistries.ITEMS.getValue(new ResourceLocation("miztinker", "witch_fiber"));

                    if (witchFiber != null) {
                        ItemStack dropStack = new ItemStack(witchFiber);

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
    }
}
