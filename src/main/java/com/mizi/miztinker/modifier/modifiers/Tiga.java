package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.client.TigaShieldRenderTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

public class Tiga extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        // 只在盔甲栏正确槽位中生效
        if (!(holder instanceof ServerPlayer player)) return;
        if (!isCorrectSlot) return;
        if (level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;

        // ===== 脚下光源逻辑 =====
        BlockPos footPos = player.blockPosition();
        BlockPos lightPos = footPos.above(); // 在脚下方块表面

        if (serverLevel.isEmptyBlock(lightPos)) {
            serverLevel.setBlock(lightPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
        }

        // ===== 光之护盾逻辑 =====
        boolean hasHeroEffect = player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);

        // 仅当穿着此盔甲 且 拥有村庄英雄效果时 才启用护盾
        if (hasHeroEffect && isCorrectSlot) {
            TigaShieldRenderTracker.setShieldActive(player, true);

            // 每5t造成范围魔法伤害
            if (player.tickCount % 5 == 0) {
                var list = player.level().getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(3), e -> e != player);
                for (var entity : list) {
                    entity.hurt(player.damageSources().magic(), 100f);
                }
            }
        } else {
            // 一旦脱下装备或失去英雄效果，立即关闭护盾
            TigaShieldRenderTracker.setShieldActive(player, false);
        }
    }
}