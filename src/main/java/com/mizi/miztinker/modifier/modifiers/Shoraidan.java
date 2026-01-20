package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.LootForceUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

import java.util.List;

public class Shoraidan extends NoLevelsModifier {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void execute(LivingEntity target, Player player) {
        if (target.level().isClientSide || !(target.level() instanceof ServerLevel serverLevel) || target instanceof Player) {
            return;
        }

        float currentHealth = target.getHealth();
        float damageAmount = 200.0f;

        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 4.0F, 1.0F);

        float luck = player.getLuck();
        List<ItemStack> generatedLoot = LootForceUtil.generateEntityLoot(target, luck, true);

        if (!generatedLoot.isEmpty()) {
            for (ItemStack stack : generatedLoot) {
                if (!stack.isEmpty()) {
                    target.spawnAtLocation(stack);
                }
            }
        }

        if (currentHealth <= damageAmount) {
            target.setHealth(0);
            target.hurt(target.damageSources().lightningBolt(), Float.MAX_VALUE);

            if (target.isAlive()) {
                target.discard();
            }
        } else {
            target.setHealth(currentHealth - damageAmount);
            target.hurt(target.damageSources().lightningBolt(), 0.1f);
        }

        target.level().playSound(null, target.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0F, 0.7F);
    }
}