package com.mizi.miztinker.recipes;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.VillagerTradesEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayList;
import java.util.List;

public class VillagerTradeHandler {

    public VillagerTradeHandler() {
        MinecraftForge.EVENT_BUS.addListener(this::onAddCustomTrades);
    }

    private void onAddCustomTrades(VillagerTradesEvent event) {

        VillagerProfession type = event.getType();
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        int level = 5;

        if (type == VillagerProfession.FARMER) {
            trades.computeIfAbsent(level, k -> new ArrayList<>());
            List<VillagerTrades.ItemListing> levelTrades = trades.get(level);

            while (levelTrades.size() < 2) {
                levelTrades.add((trader, rand) -> new MerchantOffer(
                        new ItemStack(Items.EMERALD, 1),
                        new ItemStack(Items.CARROT),
                        12, 5, 0.05f
                ));
            }

            ItemStack[] pool = new ItemStack[]{
                    new ItemStack(Items.COCOA_BEANS),
                    new ItemStack(Items.PITCHER_POD),
                    new ItemStack(Items.SEA_PICKLE),
                    new ItemStack(Items.POISONOUS_POTATO)
            };

            levelTrades.add((trader, rand) -> {
                ItemStack selected = pool[rand.nextInt(pool.length)];
                return new MerchantOffer(
                        new ItemStack(Items.EMERALD_BLOCK, 1),
                        selected,
                        1,
                        0,
                        0.1f
                );
            });
        }

        if (type == VillagerProfession.ARMORER ||
                type == VillagerProfession.WEAPONSMITH ||
                type == VillagerProfession.TOOLSMITH) {

            trades.computeIfAbsent(level, k -> new ArrayList<>());
            List<VillagerTrades.ItemListing> levelTrades = trades.get(level);

            levelTrades.add((trader, rand) -> {

                if (rand.nextFloat() > 0.75f) {
                    return null;
                }

                return new MerchantOffer(
                        new ItemStack(Items.EMERALD_BLOCK, 2),
                        new ItemStack(getGoldCoinItem(), 1),
                        10,
                        5,
                        0.05f
                );
            });
        }


            }


    private Item getGoldCoinItem() {
        return net.minecraftforge.registries.ForgeRegistries.ITEMS
                .getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("miztinker", "gold_coin"));
    }

}