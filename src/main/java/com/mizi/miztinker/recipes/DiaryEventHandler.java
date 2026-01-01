package com.mizi.miztinker.recipes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "miztinker")
public class DiaryEventHandler {

    private static final Random RANDOM = new Random();
    private static final int MESSAGE_COUNT = 15;
    private static final String TARGET_ITEM_ID = "miztinker:mirai_nikki";

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && itemId.toString().equals(TARGET_ITEM_ID)) {

            if (!event.getLevel().isClientSide) {

                int randomIndex = RANDOM.nextInt(MESSAGE_COUNT) + 1;

                String langKey = "miztinker.diary.msg." + randomIndex;
                player.sendSystemMessage(Component.translatable(langKey));

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.PLAYERS,
                        1.0f, 1.2f);
            }


            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
    }
}
