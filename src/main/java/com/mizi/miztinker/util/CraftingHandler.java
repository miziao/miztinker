package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.register.MiztinkerItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.registries.BuiltInRegistries;

@Mod.EventBusSubscriber(modid = "miztinker")
public class CraftingHandler {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();

        boolean isNugget = result.is(MiztinkerItems.SAPLING_NUGGET_ITEM.get());
        boolean isDynamax = result.is(MiztinkerItems.DYNAMAX_SAPLING_ITEM.get());

        if (isNugget || isDynamax) {
            ItemStack saplingStack = ItemStack.EMPTY;
            for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
                ItemStack stack = event.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                    if (blockItem.getBlock() instanceof SaplingBlock) {
                        saplingStack = stack;
                        break;
                    }
                }
            }

            if (!saplingStack.isEmpty()) {
                String saplingId = BuiltInRegistries.ITEM.getKey(saplingStack.getItem()).toString();
                result.getOrCreateTag().putString("SaplingType", saplingId);
            }
        }
    }
}