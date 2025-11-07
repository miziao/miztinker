package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class Sodium extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(holder instanceof Player player)) return;

        BlockPos pos = player.blockPosition();
        BlockState state = world.getBlockState(pos);

        // 检查是否在水里
        if (state.getBlock() == Blocks.WATER || state.getFluidState().is(Fluids.WATER)) {
            explode(player);
            clearWater(world, pos);
        }
    }

    /** 生成爆炸 */
    public static void explode(Player player) {
        Level level = player.level();
        final float radius = 3.0f; // 爆炸半径

        // 播放爆炸声音
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                4.0F,
                (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F
        );

        // 爆炸效果，破坏方块
        level.explode(
                player, // 爆炸来源
                player.getX(), player.getY(), player.getZ(),
                radius,
                true, // 是否破坏方块
                Level.ExplosionInteraction.MOB // 只对生物类型产生效果
        );

        // 创建 AABB 范围，半径为 radius
        AABB explosionBox = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );
        player.hurt(level.damageSources().explosion(player, player), 9.1f);
        // 获取范围内所有生物和玩家，排除掉落物

        List<Mob> entities = level.getEntitiesOfClass(
                Mob.class,
                explosionBox,
                mob -> mob != null && mob.isAlive()
        );

        // 对每个实体造成爆炸伤害
        for (LivingEntity entity : entities) {
            entity.hurt(level.damageSources().explosion(player, player), 150.0f);
        }
    }





    /** 清除 3x3x3 范围的水 */
    private void clearWater(Level world, BlockPos center) {
        BlockPos.betweenClosedStream(center.offset(-1, -1, -1), center.offset(1, 1, 1))
                .forEach(pos -> {
                    BlockState state = world.getBlockState(pos);
                    if (state.getBlock() == Blocks.WATER || state.getFluidState().is(Fluids.WATER)) {
                        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                });
    }
}