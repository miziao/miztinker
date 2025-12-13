package com.mizi.miztinker.effect;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
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
        if (level.isClientSide()) return;

        /*-----------------------------------
         *            玩家行为
         -----------------------------------*/
        if (entity instanceof Player player) {

            // ★ 强制掉血（可受伤但不会死亡）
            player.hurt(player.damageSources().magic(), 1.0F);

            // 掉落金币
            int coins = 1 + amplifier;
            for (int i = 0; i < coins; i++) {
                ItemStack stack = new ItemStack(MiztinkerItems.GOLD_COIN.get());

                double offsetX = (RANDOM.nextDouble() - 0.5) * 0.8;
                double offsetY = 0.3 + RANDOM.nextDouble() * 0.8;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.8;

                ItemEntity item = new ItemEntity(level,
                        entity.getX() + offsetX,
                        entity.getY() + entity.getBbHeight() + offsetY,
                        entity.getZ() + offsetZ,
                        stack);

                // 喷射速度
                double motionX = (RANDOM.nextDouble() - 0.5) * 1.2;
                double motionY = 0.7 + RANDOM.nextDouble() * 0.7;
                double motionZ = (RANDOM.nextDouble() - 0.5) * 1.2;

                item.setDeltaMovement(motionX, motionY, motionZ);
                level.addFreshEntity(item);
            }
            return;
        }

        /*-----------------------------------
         *          非玩家生物行为
         -----------------------------------*/
        try {
            // 造成 1 点魔法伤害触发掉落（不会意外死掉）
            entity.hurt(entity.damageSources().magic(), 1.0F);

            // 调用 dropAllDeathLoot
            Method dropLoot = ObfuscationReflectionHelper.findMethod(
                    LivingEntity.class, "m_6668_", DamageSource.class
            );
            dropLoot.setAccessible(true);
            dropLoot.invoke(entity, entity.damageSources().magic());

            // 找到新掉落物并喷射
            List<ItemEntity> items =
                    level.getEntitiesOfClass(ItemEntity.class,
                            entity.getBoundingBox().inflate(1.2));

            for (ItemEntity item : items) {
                if (item.tickCount > 1) continue; // 只处理本 tick 新掉落

                item.setPos(entity.getX(),
                        entity.getY() + entity.getBbHeight(),
                        entity.getZ());

                double motionX = (RANDOM.nextDouble() - 0.5) * 0.5;
                double motionY = 0.5 + RANDOM.nextDouble() * 0.3;
                double motionZ = (RANDOM.nextDouble() - 0.5) * 0.5;

                item.setDeltaMovement(motionX, motionY, motionZ);
            }

        } catch (Exception e) {
            throw new RuntimeException("调用 dropAllDeathLoot 失败", e);
        }
    }
}