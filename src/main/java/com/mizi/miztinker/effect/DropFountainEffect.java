package com.mizi.miztinker.effect;

import com.mizi.miztinker.modifier.modifiers.base.LootForceUtil;
import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Random;

public class DropFountainEffect extends MobEffect {

    private static final Random RANDOM = new Random();

    public DropFountainEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFFAA00);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 2 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        if (entity instanceof Player player) {
            player.hurt(player.damageSources().magic(), 1.0F);

            int coins = 1 + amplifier;
            for (int i = 0; i < coins; i++) {
                ItemStack stack = new ItemStack(MiztinkerItems.GOLD_COIN.get());
                spawnItemWithVelocity(serverLevel, entity, stack, 1.2, 0.7);
            }
            return;
        }

        entity.hurt(entity.damageSources().magic(), 1.0F);

        float luck = (entity instanceof Player p) ? p.getLuck() : 0.0F;

        List<ItemStack> generatedLoot = LootForceUtil.generateEntityLoot(entity, luck, true);

        if (!generatedLoot.isEmpty()) {
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
                    entity.getBoundingBox().inflate(1.5));

            for (ItemEntity item : items) {
                if (item.tickCount <= 1) {
                    double motionX = (RANDOM.nextDouble() - 0.5) * 0.6;
                    double motionY = 0.6 + RANDOM.nextDouble() * 0.5;
                    double motionZ = (RANDOM.nextDouble() - 0.5) * 0.6;

                    item.setDeltaMovement(motionX, motionY, motionZ);
                    // 标记已被处理，防止重复赋予速度
                    item.hasImpulse = true;
                }
            }
        }
    }

    private void spawnItemWithVelocity(Level level, LivingEntity entity, ItemStack stack, double horizontalScale, double verticalBase) {
        double offsetX = (RANDOM.nextDouble() - 0.5) * 0.8;
        double offsetY = 0.3 + RANDOM.nextDouble() * 0.5;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.8;

        ItemEntity item = new ItemEntity(level,
                entity.getX() + offsetX,
                entity.getY() + entity.getBbHeight() * 0.8,
                entity.getZ() + offsetZ,
                stack);

        double motionX = (RANDOM.nextDouble() - 0.5) * horizontalScale;
        double motionY = verticalBase + RANDOM.nextDouble() * 0.5;
        double motionZ = (RANDOM.nextDouble() - 0.5) * horizontalScale;

        item.setDeltaMovement(motionX, motionY, motionZ);
        level.addFreshEntity(item);
    }
}