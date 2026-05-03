package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurt;

public class Agni extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final int DAMAGE_COOLDOWN_TICKS = 10;
    private static final float SELF_DAMAGE = 10f;
    private static final float SPREAD_DAMAGE = 10f;
    private static final double RADIUS = 1.5;

    private static final String TAG_AGNI_BURN = "agni_eternal_burn";
    private static final String TAG_COOLDOWN = "agni_damage_cooldown";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier,
                                Level level, LivingEntity holder,
                                int slot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (!(holder instanceof ServerPlayer player) || !isCorrectSlot || level.isClientSide()) return;

        ServerLevel server = (ServerLevel) level;
        CompoundTag persistentData = player.getPersistentData();

        player.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 100, true, false, false));

        if (!player.isInWaterOrRain()) {
            player.setSecondsOnFire(10);
        }

        int cooldown = persistentData.getInt(TAG_COOLDOWN);
        boolean isOwnerOnFire = player.isOnFire();

        if (isOwnerOnFire) {
            if (cooldown <= 0) {
                forceHurt(player, player.damageSources().generic(), SELF_DAMAGE);

                processNearbyEntities(server, player);

                persistentData.putInt(TAG_COOLDOWN, DAMAGE_COOLDOWN_TICKS);
            } else {
                persistentData.putInt(TAG_COOLDOWN, cooldown - 1);
            }
        }

        BlockPos pos = player.blockPosition();
        if (server.isEmptyBlock(pos) && !player.isInWaterOrRain()) {
            server.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        }

        handleEternalBurningEntities(server, player);
    }

    private void processNearbyEntities(ServerLevel server, ServerPlayer player) {
        List<LivingEntity> nearby = server.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(RADIUS),
                e -> e != player && e.isAlive()
        );

        for (LivingEntity target : nearby) {
            if (!target.getPersistentData().contains(TAG_AGNI_BURN)) {
                target.getPersistentData().putBoolean(TAG_AGNI_BURN, true);
            }

            forceHurt(target, player.damageSources().generic(), SPREAD_DAMAGE);
        }
    }

    private void handleEternalBurningEntities(ServerLevel server, ServerPlayer player) {
        List<LivingEntity> affected = server.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(16),
                e -> e.getPersistentData().getBoolean(TAG_AGNI_BURN) && e.isAlive()
        );

        for (LivingEntity target : affected) {
            if (!target.isOnFire()) {
                target.setSecondsOnFire(5);
            }

            if (player.getPersistentData().getInt(TAG_COOLDOWN) == DAMAGE_COOLDOWN_TICKS) {
                forceHurt(target, player.damageSources().generic(), SPREAD_DAMAGE);
            }

        }
    }
}