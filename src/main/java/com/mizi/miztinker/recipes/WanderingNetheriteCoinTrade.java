package com.mizi.miztinker.recipes;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.VillagerTrades;
import org.jetbrains.annotations.Nullable;

public enum WanderingNetheriteCoinTrade implements VillagerTrades.ItemListing {

    INSTANCE;

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {

        // 70%（或你设的概率）出现
        if (random.nextFloat() > 1.0f) {
            return null; // 不生成这个交易
        }

        Item coin = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation("miztinker", "netherite_coin")
        );

        if (coin == null) return null;

        return new MerchantOffer(
                new ItemStack(Items.EMERALD_BLOCK, 10), // 玩家支付
                new ItemStack(coin),                    // 下界合金币
                3,                                      // maxUses
                0,                                      // no XP
                0.05f
        );
    }
}