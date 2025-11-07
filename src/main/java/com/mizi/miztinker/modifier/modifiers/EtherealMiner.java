package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockHarvestModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

public class EtherealMiner extends NoLevelsModifier implements BlockHarvestModifierHook {



    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_HARVEST);
    }

    public void startHarvest(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        ServerLevel world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockPos abovePos = pos.above();

        BlockState aboveState = world.getBlockState(abovePos);
        Block aboveBlock = aboveState.getBlock();

        if (aboveBlock instanceof FallingBlock) {
            Block soulGlass = TinkerSmeltery.scorchedSoulGlass.get(); // 正确引用
            world.setBlock(abovePos, soulGlass.defaultBlockState(), 3); // 放置阻止重力方块掉落

            // 放置 scorched_soul_glass 阻止掉落
            world.setBlock(abovePos, soulGlass.defaultBlockState(), 3);
        }
    }

    @Override
    public void finishHarvest(IToolStackView iToolStackView, ModifierEntry modifierEntry, ToolHarvestContext toolHarvestContext, int i) {

    }
}