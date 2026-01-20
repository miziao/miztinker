package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class Atomslash extends NoLevelsModifier implements LeftClickModifierHook {

    private static final int GRID_X = 5;
    private static final int GRID_Y = 5;
    private static final float GAP = 1.0f;
    private static final int COLOR = 0xFF4444;


    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, MiztinkerHooks.LEFT_CLICK);
    }

    @Override
    public void onLeftClickEmpty(IToolStackView tool, ModifierEntry entry, Player player, Level world, EquipmentSlot slot) {
        if (!world.isClientSide) {
            spawnGridSwords(tool, entry, player, world);
        }
    }

    @Override
    public void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level world,
                                 EquipmentSlot slot, BlockState state, BlockPos pos) {
        if (!world.isClientSide) {
            spawnGridSwords(tool, entry, player, world);
        }
    }

    private void spawnGridSwords(IToolStackView tool, ModifierEntry entry, Player player, Level world) {
        Vec3 center = player.position().add(0, player.getBbHeight() * 0.5, 0);

        for (int i = 0; i < GRID_X; i++) {
            for (int j = 0; j < GRID_Y; j++) {
                double xOffset = (i - (double) GRID_X / 2) * GAP;
                double yOffset = (j - (double) GRID_Y / 2) * GAP;
                Vec3 spawnPos = center.add(xOffset, yOffset, 0);

                EntitySlashEffect sword = new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, world);
                sword.setOwner(player);
                sword.setDamage(0.1f);
                sword.setIsCritical(true);
                sword.setColor(COLOR);
                sword.setCycleHit(false);
                sword.setIndirect(true);

                float yaw = player.getYRot();
                float pitch = (float) Math.toDegrees(Math.atan2(yOffset, GAP));
                sword.absMoveTo(spawnPos.x, spawnPos.y, spawnPos.z, yaw, pitch);

                world.addFreshEntity(sword);
            }
        }

        AABB area = new AABB(
                player.getX() - GRID_X * GAP,
                player.getY() - GRID_Y * GAP,
                player.getZ() - GRID_X * GAP,
                player.getX() + GRID_X * GAP,
                player.getY() + GRID_Y * GAP,
                player.getZ() + GRID_X * GAP
        );

        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());
        for (LivingEntity target : entities) {
            target.hurt(player.damageSources().playerAttack(player), target.getHealth() / 2.0f);
        }

        player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 0.3F, 1.3F);
    }
}