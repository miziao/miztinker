package com.mizi.miztinker.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = "miztinker")
public class SleepTransformHandler {

    private static final double CHANCE = 0.0001;

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        long time = level.getDayTime() % 24000;
        boolean isMorning = time >= 0 && time < 1000;

        if (!isMorning) {
            return;
        }

        boolean transformed = tryTransformBook(player, serverLevel);

        if (transformed) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1, player.getZ(),
                    60, 0.5, 0.5, 0.5, 0.15);

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 1.0f, 0.8f);

            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§d这并不是一场梦, 拿着这份力量赢得这场生存游戏吧"), true);
        }
    }

    private static boolean tryTransformBook(Player player, ServerLevel level) {
        boolean anyTransformed = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.WRITTEN_BOOK) {
                if (level.random.nextDouble() < CHANCE) {
                    ItemStack newStack = new ItemStack(
                            Objects.requireNonNull(
                                    level.registryAccess()
                                            .registryOrThrow(Registries.ITEM)
                                            .get(new ResourceLocation("miztinker:mirai_nikki"))
                            ),
                            stack.getCount()
                    );
                    player.getInventory().setItem(i, newStack);
                    anyTransformed = true;
                }
            }
        }
        return anyTransformed;
    }
}