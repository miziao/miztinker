package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockHarvestModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import net.minecraft.sounds.SoundEvents;

import java.util.List;


public class Eatstone extends NoLevelsModifier implements  BlockHarvestModifierHook {

    private static final int FOOD_STONE = 1;
    private static final float SAT_STONE = 0.05f;
    private static final int FOOD_ORE = 3;
    private static final float SAT_ORE = 0.3f;

    /**
     * 阶段1：在方块移除前控制是否掉落。
     * 返回 true 表示我们处理了破坏逻辑，匠魂不会再执行默认掉落。
     */
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this,ModifierHooks.BLOCK_HARVEST);
    }

    /**
     * 阶段1：移除方块并准备清理掉落物
     */
    @Override
    public void finishHarvest(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context, int level) {
        Player player = context.getPlayer();
        if (player == null || player.level().isClientSide) return;

        if (!context.isEffective()) return;

        BlockPos pos = context.getPos();
        BlockState state = context.getState();
        if (pos == null || state == null) return;

        Level world = player.level();
        int food = 0;
        float saturation = 0f;

        // 判断方块类型
        if (state.is(BlockTags.create(new ResourceLocation("forge", "stone")))) {
            food = FOOD_STONE;
            saturation = SAT_STONE;
        } else if (state.is(BlockTags.create(new ResourceLocation("forge", "ores")))) {
            food = FOOD_ORE;
            saturation = SAT_ORE;
        }

        if (food > 0) {
            // 播放破坏粒子
            world.levelEvent(2001, pos, Block.getId(state));

            // 延迟几tick清理掉落物（让方块有时间生成掉落物）
            world.scheduleTick(pos, state.getBlock(), 1);

            // 手动清理掉落物（在当前 tick 或下一 tick）
            AABB box = new AABB(pos).inflate(1.0);
            List<ItemEntity> drops = world.getEntitiesOfClass(ItemEntity.class, box);
            for (ItemEntity item : drops) {
                item.discard(); // 直接删除
            }

            // 播放吃东西音效（吃+打嗝）
            world.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
            world.playSound(null, pos, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 1.0F);

            // 恢复饥饿与饱和度
            player.getFoodData().eat(food, saturation);
        }
    }
}