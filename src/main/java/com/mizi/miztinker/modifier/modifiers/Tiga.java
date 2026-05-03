package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.client.TigaShieldRenderTracker;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
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
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class Tiga extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (!(holder instanceof ServerPlayer player)) return;
        if (!isCorrectSlot) return;
        if (level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;

        BlockPos footPos = player.blockPosition();
        BlockPos lightPos = footPos.above();

        if (serverLevel.isEmptyBlock(lightPos)) {
            serverLevel.setBlock(lightPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
        }

        boolean hasHeroEffect = player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);

        if (hasHeroEffect) {
            TigaShieldRenderTracker.setShieldActive(player, true);

            if (player.tickCount % 5 == 0) {
                var list = player.level().getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(3), e -> e != player);
                for (var entity : list) {
                    entity.hurt(player.damageSources().magic(), 100f);
                }
            }
        } else {
            TigaShieldRenderTracker.setShieldActive(player, false);
        }
    }
}