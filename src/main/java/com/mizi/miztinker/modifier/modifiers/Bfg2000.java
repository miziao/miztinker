package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;


import java.util.Random;

public class Bfg2000 extends NoLevelsModifier implements GeneralInteractionModifierHook {

    private static final int DURABILITY_LOSS = 200;      // 每次使用消耗耐久
    private static final float LASER_LENGTH = 90.0f;    // 激光长度
    private static final double STEP = 0.5;             // 每 0.5 格检测一次

    private final Random random = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    /** 右键触发激光 */
    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player,
                                       InteractionHand hand, InteractionSource source) {
        Level level = player.level();
        ItemStack stack = player.getItemInHand(hand);

        float maxDurability = tool.getStats().get(ToolStats.DURABILITY);
        float currentDurability = maxDurability - tool.getDamage();

        if (currentDurability <= 0) {
            if (level.isClientSide) {
                player.displayClientMessage(Component.literal("§7耐久不足，无法使用BFG激光！"), true);
            }
            return InteractionResult.PASS;
        }

        // 消耗耐久
        ToolDamageUtil.damage(tool, DURABILITY_LOSS, player, stack);

        if (!level.isClientSide) {
            shootLaserServer(level, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.0f);
        } else {
            shootLaserClient(level, player);
        }

        return InteractionResult.SUCCESS;
    }

    /** 服务端：伤害 + 烧毁方块 */
    private void shootLaserServer(Level level, Player player) {
        Vec3 start = player.getEyePosition().add(player.getLookAngle().scale(1.5)); // 稍微前移
        Vec3 look = player.getLookAngle().normalize();

        for (double d = 0; d < LASER_LENGTH; d += STEP) {
            Vec3 current = start.add(look.scale(d));

            // --- 实体伤害 ---



            // --- 方块破坏 (3x3 区域) ---
            BlockPos center = BlockPos.containing(current);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos target = center.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(target);

                        if (state.isAir() || !state.getFluidState().isEmpty()) continue;
                        if (state.getDestroySpeed(level, target) < 0) continue;

                        level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /** 客户端：高能绿色粒子激光 */
    private void shootLaserClient(Level level, Player player) {
        Vec3 start = player.getEyePosition().add(player.getLookAngle().scale(1.5));
        Vec3 look = player.getLookAngle().normalize();

        int steps = (int) (LASER_LENGTH / STEP);
        for (int i = 0; i < steps; i++) {
            Vec3 basePos = start.add(look.scale(i * STEP));

            for (int j = 0; j < 4; j++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.2;
                double offsetY = (random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (random.nextDouble() - 0.5) * 0.2;

                DustParticleOptions particle = new DustParticleOptions(new Vector3f(0f, 1f, 0f), 2.0f);
                level.addParticle(particle,
                        basePos.x + offsetX,
                        basePos.y + offsetY,
                        basePos.z + offsetZ,
                        0, 0, 0);
            }
        }
    }
}