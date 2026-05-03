package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class LoveStone extends NoLevelsModifier implements LeftClickModifierHook {

    @Override
    public void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot equipmentSlot, BlockState state, BlockPos pos) {
        if (level.isClientSide) return;

        if (state.is(Blocks.BEDROCK)) {
            if (level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())) {
                ItemStack bedrockStack = new ItemStack(Blocks.BEDROCK);
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        bedrockStack
                );
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
            return;
        }

        if (state.is(BlockTags.STONE_ORE_REPLACEABLES) ||
                state.is(BlockTags.BASE_STONE_OVERWORLD) ||
                state.is(BlockTags.BASE_STONE_NETHER) ||
                state.is(Blocks.END_STONE) ||
                state.is(Blocks.COBBLESTONE) ||
                state.is(Blocks.STONE) ||
                state.is(Blocks.DEEPSLATE) ||
                state.is(Blocks.TUFF)
        ) {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, MiztinkerHooks.LEFT_CLICK);
    }
}