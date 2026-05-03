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


        if (random.nextFloat() > 1.0f) {
            return null;
        }

        Item coin = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("miztinker", "netherite_coin")
        );

        if (coin == null) return null;

        return new MerchantOffer(
                new ItemStack(Items.EMERALD_BLOCK, 10),
                new ItemStack(coin),
                3,
                0,
                0.05f
        );
    }
}