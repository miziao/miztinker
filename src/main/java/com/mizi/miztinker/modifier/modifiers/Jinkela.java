package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Optional;

public class Jinkela extends NoLevelsModifier implements BlockInteractionModifierHook, InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        Player player = context.getPlayer();
        if (player == null || player.getFoodData().getFoodLevel() <= 0) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (applyJinkelaEffect(level, pos, player)) {
            if (!level.isClientSide) {
                player.getFoodData().addExhaustion(3.0F);
                player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectHoldSlot, ItemStack stack) {
        if (!level.isClientSide && isSelected && holder.isShiftKeyDown() && holder.tickCount % 20 == 0) {
            if (holder instanceof Player player) {
                if (player.getFoodData().getFoodLevel() < 2) return;
                BlockPos center = player.blockPosition();

                for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
                    applyJinkelaEffect(level, pos.immutable(), player);
                }

                player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 2);
                player.getFoodData().addExhaustion(6.0F);
            }
        }
    }

    private boolean applyJinkelaEffect(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        boolean success = false;

        if (block instanceof SugarCaneBlock || block instanceof CactusBlock) {
            for (int i = 1; i < 3; i++) {
                BlockPos target = pos.above(i);
                if (level.isEmptyBlock(target)) {
                    level.setBlockAndUpdate(target, block.defaultBlockState());
                    success = true;
                    break;
                }
            }
        }
        else if (block instanceof StemBlock) {
            int age = state.getValue(StemBlock.AGE);
            if (age < StemBlock.MAX_AGE) {
                level.setBlockAndUpdate(pos, state.setValue(StemBlock.AGE, age + 1));
                success = true;
            } else {
                Block fruitBlock = (block == Blocks.PUMPKIN_STEM) ? Blocks.PUMPKIN : Blocks.MELON;
                boolean fruitFound = false;

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos fruitPos = pos.relative(dir);
                    BlockState fruitState = level.getBlockState(fruitPos);

                    if (fruitState.is(fruitBlock)) {
                        if (!level.isClientSide) {
                            harvestBlock(level, fruitPos, fruitState, player);
                        }
                        fruitFound = true;
                        success = true;
                        break;
                    }
                }

                if (!fruitFound) {
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos targetPos = pos.relative(dir);
                        if (level.isEmptyBlock(targetPos) && level.getBlockState(targetPos.below()).canOcclude()) {
                            level.setBlockAndUpdate(targetPos, fruitBlock.defaultBlockState());
                            success = true;
                            break;
                        }
                    }
                }
            }
        }
        else {
            Optional<Property<?>> agePropOpt = state.getProperties().stream()
                    .filter(p -> p.getName().equals("age") && p instanceof IntegerProperty)
                    .findFirst();

            if (agePropOpt.isPresent()) {
                IntegerProperty ageProp = (IntegerProperty) agePropOpt.get();
                int maxAge = ageProp.getPossibleValues().stream().mapToInt(v -> v).max().orElse(0);
                int currentAge = state.getValue(ageProp);

                if (currentAge >= maxAge) {
                    if (!level.isClientSide) {
                        harvestBlock(level, pos, state, player); // 收割
                        level.setBlockAndUpdate(pos, state.setValue(ageProp, 0)); // 补种
                    }
                    success = true;
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(ageProp, currentAge + 1));
                    success = true;
                }
            }
        }

        if (!success) {
            ItemStack dummyBoneMeal = new ItemStack(net.minecraft.world.item.Items.BONE_MEAL);
            if (net.minecraft.world.item.BoneMealItem.applyBonemeal(dummyBoneMeal, level, pos, player)) {
                success = true;
            }
        }

        if (success && !level.isClientSide) {
            ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.05);
        }

        return success;
    }

    private void harvestBlock(Level level, BlockPos pos, BlockState state, Player player) {
        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, pos, null, player, player.getMainHandItem());
        for (ItemStack drop : drops) {
            Block.popResource(level, pos, drop);
        }
        ((ServerLevel) level).sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, state),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.05);
    }
}