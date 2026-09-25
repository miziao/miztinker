package com.mizi.miztinker.recipes;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class YoruninaParrotHandler {

    private static final String TAG_YORUNINA_TIMER = "miztinker_yorunina_timer";
    private static final String TAG_YORUNINA_ACTIVE = "miztinker_yorunina_active";

    private static final int EXPLODE_DELAY = 100;

    private static final float EXPLOSION_POWER = 2.0F;

    @SubscribeEvent
    public static void onRightClickParrot(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) {
            return;
        }

        Entity target = event.getTarget();

        if (!(target instanceof Parrot parrot)) {
            return;
        }

        Player player = event.getEntity();
        ItemStack held = event.getItemStack();

        if (!held.is(Items.MILK_BUCKET)) {
            return;
        }

        CompoundTag data = parrot.getPersistentData();

        if (data.getBoolean(TAG_YORUNINA_ACTIVE)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        data.putBoolean(TAG_YORUNINA_ACTIVE, true);
        data.putInt(TAG_YORUNINA_TIMER, EXPLODE_DELAY);

        parrot.playSound(SoundEvents.PARROT_EAT, 1.0F, 0.6F);

        consumeMilkBucket(player, held, event.getHand());

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Parrot parrot)) {
            return;
        }

        if (parrot.level().isClientSide) {
            return;
        }

        CompoundTag data = parrot.getPersistentData();

        if (!data.getBoolean(TAG_YORUNINA_ACTIVE)) {
            return;
        }

        int timer = data.getInt(TAG_YORUNINA_TIMER);

        if (timer > 0) {
            data.putInt(TAG_YORUNINA_TIMER, timer - 1);
            return;
        }

        explodeAndDropYorunina(parrot);
    }

    private static void consumeMilkBucket(Player player, ItemStack held, InteractionHand hand) {
        if (player.getAbilities().instabuild) {
            return;
        }

        held.shrink(1);

        ItemStack bucket = new ItemStack(Items.BUCKET);

        if (held.isEmpty()) {
            player.setItemInHand(hand, bucket);
        } else if (!player.getInventory().add(bucket)) {
            player.drop(bucket, false);
        }
    }

    private static void explodeAndDropYorunina(Parrot parrot) {
        if (!(parrot.level() instanceof ServerLevel level)) {
            return;
        }

        double x = parrot.getX();
        double y = parrot.getY();
        double z = parrot.getZ();

        parrot.discard();

        level.explode(
                parrot,
                x,
                y,
                z,
                EXPLOSION_POWER,
                false,
                net.minecraft.world.level.Level.ExplosionInteraction.BLOCK
        );

        ItemStack yorunina = new ItemStack(MiztinkerItems.YORUNINA.get());

        ItemEntity itemEntity = new ItemEntity(
                level,
                x,
                y + 0.25D,
                z,
                yorunina
        );

        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }
}