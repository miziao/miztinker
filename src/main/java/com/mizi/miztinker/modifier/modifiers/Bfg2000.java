package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Random;

public class Bfg2000 extends NoLevelsModifier implements GeneralInteractionModifierHook {

    private static final int DURABILITY_LOSS = 200;
    private static final float LASER_LENGTH = 90.0f;
    private static final double STEP = 0.5;
    private static final float DAMAGE_PER_HIT = 20.0f;

    private static final String KEY_BROKEN = "modifier.miztinker.bfg2000.broken";
    private static final String KEY_NO_ENERGY = "modifier.miztinker.bfg2000.no_energy";

    private final Random random = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player,
                                       InteractionHand hand, InteractionSource source) {
        Level level = player.level();
        ItemStack stack = player.getItemInHand(hand);

        if (tool.isBroken()) {
            if (level.isClientSide) {
                // 使用 translatable 调用本地化键名
                player.displayClientMessage(Component.translatable(KEY_BROKEN), true);
            }
            return InteractionResult.PASS;
        }

        if (tool.getCurrentDurability() < DURABILITY_LOSS) {
            if (level.isClientSide) {
                // 使用 translatable 调用本地化键名
                player.displayClientMessage(Component.translatable(KEY_NO_ENERGY), true);
            }
            return InteractionResult.PASS;
        }

        ToolDamageUtil.damage(tool, DURABILITY_LOSS, player, stack);

        if (!level.isClientSide) {
            shootLaserServer(level, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.2f);
        } else {
            shootLaserClient(level, player);
        }

        return InteractionResult.SUCCESS;
    }

    private void shootLaserServer(Level level, Player player) {
        Vec3 start = player.getEyePosition().add(player.getLookAngle().scale(1.2));
        Vec3 look = player.getLookAngle().normalize();

        for (double d = 0; d < LASER_LENGTH; d += STEP) {
            Vec3 current = start.add(look.scale(d));

            AABB hitbox = new AABB(current.x - 0.8, current.y - 0.8, current.z - 0.8,
                    current.x + 0.8, current.y + 0.8, current.z + 0.8);

            List<Entity> entities = level.getEntities(player, hitbox);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity living && entity.isAlive()) {
                    living.invulnerableTime = 0;
                    living.hurt(level.damageSources().playerAttack(player), DAMAGE_PER_HIT);
                    living.invulnerableTime = 0;

                    living.setSecondsOnFire(3);
                }
            }

            BlockPos center = BlockPos.containing(current);
            if (d % 1.0 == 0) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos target = center.offset(dx, dy, dz);
                            BlockState state = level.getBlockState(target);
                            if (state.isAir()) continue;
                            if (state.getDestroySpeed(level, target) < 0) continue;
                            level.destroyBlock(target, false, player);
                        }
                    }
                }
            }
        }
    }

    private void shootLaserClient(Level level, Player player) {
        Vec3 start = player.getEyePosition().add(player.getLookAngle().scale(1.2));
        Vec3 look = player.getLookAngle().normalize();

        int steps = (int) (LASER_LENGTH / STEP);
        for (int i = 0; i < steps; i++) {
            Vec3 basePos = start.add(look.scale(i * STEP));

            for (int j = 0; j < 3; j++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.15;
                double offsetY = (random.nextDouble() - 0.5) * 0.15;
                double offsetZ = (random.nextDouble() - 0.5) * 0.15;

                DustParticleOptions particle = new DustParticleOptions(new Vector3f(0.1f, 1.0f, 0.1f), 1.5f);
                level.addParticle(particle,
                        basePos.x + offsetX,
                        basePos.y + offsetY,
                        basePos.z + offsetZ,
                        0, 0, 0);
            }
        }
    }
}