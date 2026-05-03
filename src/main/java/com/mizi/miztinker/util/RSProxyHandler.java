package com.mizi.miztinker.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class RSProxyHandler {

    @SubscribeEvent
    public static void onContainerClosed(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restoreTinkerTool(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            if (player.tickCount % 20 == 0) {
                restoreTinkerTool(player);
            }
        }
    }

    private static void restoreTinkerTool(ServerPlayer player) {
        boolean changed = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getTag() != null && !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean("is_tinker_proxy")) {
                player.getInventory().setItem(i, convertBack(stack));
                changed = true;
            }
        }
        if (changed) {
            player.containerMenu.broadcastChanges();
        }
    }

    private static ItemStack convertBack(ItemStack proxyStack) {
        CompoundTag tag = proxyStack.getOrCreateTag();
        String originalId = tag.getString("original_tinker_id");
        var originalItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(originalId));

        ItemStack originalStack = new ItemStack(originalItem != null ? originalItem : proxyStack.getItem());
        CompoundTag newTag = tag.copy();
        newTag.remove("is_tinker_proxy");
        newTag.remove("original_tinker_id");
        newTag.remove("Type");

        originalStack.setTag(newTag);
        return originalStack;
    }
}