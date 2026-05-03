package com.mizi.miztinker.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class Paradox_Manyullyn extends Item {
    public Paradox_Manyullyn() {
        super((new Item.Properties()).stacksTo(64).rarity(Rarity.RARE));
    }

    @Override
    public Component getName(ItemStack stack) {
        if (isInPlayerInventoryGUI()) {
            return Component.translatable("item.miztinker.paradox_manyullyn.inventory_name");
        }
        return Component.translatable("item.miztinker.paradox_manyullyn.default_name");
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isInPlayerInventoryGUI() {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen instanceof InventoryScreen
                && !(mc.screen instanceof ContainerScreen);
    }

}
