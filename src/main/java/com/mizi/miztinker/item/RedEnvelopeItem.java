package com.mizi.miztinker.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class RedEnvelopeItem extends Item {

    public RedEnvelopeItem() {
        super(new Item.Properties()
                .rarity(Rarity.UNCOMMON)
                .stacksTo(64));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                executeRedEnvelopeLogic(player, level);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    private void executeRedEnvelopeLogic(Player player, Level level) {
        Random rand = new Random();
        int roll = rand.nextInt(1000);

        BlockPos pos = player.blockPosition();

        if (roll < 1) {
            spawnItem(level, pos, "miztinker:ruby_gem_old", 10);
            sendMsg(player, "miztinker.red_envelope.jackpot", ChatFormatting.GOLD);
        } else if (roll < 20) {
            spawnItem(level, pos, "miztinker:ruby_gem_old", 1);
            sendMsg(player, "miztinker.red_envelope.ruby", ChatFormatting.RED);
        } else if (roll < 100) {
            spawnItem(level, pos, "minecraft:diamond", 1);
            sendMsg(player, "miztinker.red_envelope.diamond", ChatFormatting.AQUA);
        } else if (roll < 300) {
            spawnItem(level, pos, "minecraft:emerald", 1);
            sendMsg(player, "miztinker.red_envelope.emerald", ChatFormatting.GREEN);
        } else if (roll < 500) {
            spawnItem(level, pos, "minecraft:amethyst_shard", 1);
            sendMsg(player, "miztinker.red_envelope.amethyst", ChatFormatting.LIGHT_PURPLE);
        } else if (roll < 750) {
            spawnItem(level, pos, "minecraft:copper_ingot", 1);
            sendMsg(player, "miztinker.red_envelope.copper", ChatFormatting.GOLD);
        } else {
            spawnExp(level, player, 5);
            sendMsg(player, "miztinker.red_envelope.exp", ChatFormatting.YELLOW);
        }
    }

    private void spawnItem(Level level, BlockPos pos, String registryName, int count) {
        net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(registryName);
        Item item = ForgeRegistries.ITEMS.getValue(rl);

        if (item != null && item != Items.AIR) {
            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, new ItemStack(item, count));
            level.addFreshEntity(entity);
        }
    }

    private void spawnExp(Level level, Player player, int amount) {
        ExperienceOrb orb = new ExperienceOrb(level, player.getX(), player.getY(), player.getZ(), amount);
        level.addFreshEntity(orb);
    }

    private void sendMsg(Player player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }
}