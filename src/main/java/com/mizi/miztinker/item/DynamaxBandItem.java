package com.mizi.miztinker.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DynamaxBandItem extends Item {

    public DynamaxBandItem(Properties properties) {
        super(properties);
    }

    /** 告诉游戏：这个物品在合成后会返还容器 */
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }
}