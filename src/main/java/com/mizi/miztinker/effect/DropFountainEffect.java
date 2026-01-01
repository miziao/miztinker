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
        // 只在服务端处理逻辑
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;

        if (entity instanceof Player player) {
            // 强制掉血（增加真实感，但不致死）
            player.hurt(player.damageSources().magic(), 1.0F);

            int coins = 1 + amplifier;
            for (int i = 0; i < coins; i++) {
                ItemStack stack = new ItemStack(MiztinkerItems.GOLD_COIN.get());
                spawnItemWithVelocity(serverLevel, entity, stack, 1.2, 0.7);
            }
            return;
        }

        // 1. 触发受击动作
        entity.hurt(entity.damageSources().magic(), 1.0F);

        // 2. 调用工具类生成战利品
        // 注意：LootForceUtil.generateEntityLoot 内部已经调用了 spawnAtLocation
        // 我们需要获取生成的物品并给予向上的喷射速度
        float luck = (entity instanceof Player p) ? p.getLuck() : 0.0F;

        // 获取当前位置附近的 ItemEntity，记录处理前的状态
        List<ItemStack> generatedLoot = LootForceUtil.generateEntityLoot(entity, luck, true);

        // 3. 捕捉刚刚生成的掉落物并赋予“喷泉”初速度
        if (!generatedLoot.isEmpty()) {
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
                    entity.getBoundingBox().inflate(1.5));

            for (ItemEntity item : items) {
                // 只处理本 tick 新生成的、且没有速度的掉落物
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

    /**
     * 统一的物品喷射生成方法（用于金币逻辑）
     */
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