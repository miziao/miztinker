package com.mizi.miztinker.item;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class BottledCryingObsidianItem extends Item {

    public BottledCryingObsidianItem(Properties properties) {
        super(properties);
    }

    public static void performRitual(ItemStack stack, Player player, Villager villager) {
        Level level = villager.level();

        if (!level.isClientSide) {
            level.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                    SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.NEUTRAL, 1.0F, 1.0F);

            level.broadcastEntityEvent(villager, (byte) 16);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            Timer timer = new Timer();
            AtomicInteger elapsed = new AtomicInteger(0);

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (player.getServer() != null) {
                        player.getServer().execute(() -> {
                            if (!villager.isAlive() || villager.isRemoved()) {
                                timer.cancel();
                                return;
                            }

                            float currentYaw = villager.getYRot();
                            villager.setYRot(currentYaw + 40.0F);
                            villager.setYHeadRot(villager.getYRot());
                            villager.setYBodyRot(villager.getYRot());

                            if (elapsed.addAndGet(50) >= 3000) {
                                level.explode(null, villager.getX(), villager.getY(), villager.getZ(), 3.0F, Level.ExplosionInteraction.MOB);
                                villager.spawnAtLocation(new ItemStack(MiztinkerItems.IMITATION_BROKEN_HALO.get()));
                                villager.discard();
                                timer.cancel();
                            }
                        });
                    } else {
                        timer.cancel();
                    }
                }
            }, 0, 50);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) return bottle;
            if (!player.getInventory().add(bottle)) player.drop(bottle, false);
        }
        return result;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.miztinker.bottled_crying_obsidian").withStyle(ChatFormatting.GRAY));
    }
}