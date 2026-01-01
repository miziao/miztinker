package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class Plumber extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final double MIN_FALL_SPEED = 0.5;
    private static final double BOUNCE_UP = 1.0;
    private static final float DAMAGE = 10000f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(holder instanceof Player player)) return;

        if (!isCorrectSlot) return;

        if (player.isFallFlying() || player.onGround()) return;

        Vec3 motion = player.getDeltaMovement();

        if (motion.y > -MIN_FALL_SPEED) return;

        List<LivingEntity> entities = world.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(0.5, 0.2, 0.5),
                e -> e != player && (player.getY() > e.getY() + e.getBbHeight() * 0.4)
        );

        if (!entities.isEmpty()) {
            for (LivingEntity entity : entities) {
                if (!world.isClientSide) {
                    entity.hurt(player.damageSources().playerAttack(player), DAMAGE);

                    player.setDeltaMovement(motion.x, BOUNCE_UP, motion.z);
                    player.hurtMarked = true;

                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                break;
            }
        }
    }
}