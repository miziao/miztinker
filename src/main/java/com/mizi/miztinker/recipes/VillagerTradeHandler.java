package com.mizi.miztinker.recipes;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.VillagerTradesEvent;


import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;


public class VillagerTradeHandler {


    public VillagerTradeHandler() {
        MinecraftForge.EVENT_BUS.addListener(this::onAddCustomTrades);
    }


    /**
     * 农夫大师级新增交易：从 4 个物品中随机抽 1 个，以 1 绿宝石块出售
     */
    private void onAddCustomTrades(VillagerTradesEvent event) {


// 只对农夫职业添加交易
        if (event.getType() != VillagerProfession.FARMER) return;


        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        int villagerLevel = 5; // 大师级


// 四个等概率物品池
        ItemStack[] pool = new ItemStack[] {
                new ItemStack(Items.COCOA_BEANS),
                new ItemStack(Items.PITCHER_POD),
                new ItemStack(Items.SEA_PICKLE),
                new ItemStack(Items.POISONOUS_POTATO)
        };


        trades.get(villagerLevel).add((trader, rand) -> {
// 随机选择一个物品
            ItemStack selected = pool[rand.nextInt(pool.length)];


            return new MerchantOffer(
                    new ItemStack(Items.EMERALD_BLOCK, 1), // 售价：1 绿宝石块
                    selected, // 随机物品
                    1, // maxUses
                    0, // xp
                    0.1f // priceMultiplier
            );
        });
    }
}