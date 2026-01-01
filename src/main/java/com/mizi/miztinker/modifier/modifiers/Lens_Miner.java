package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Lens_Miner extends NoLevelsModifier implements BlockBreakModifierHook {

    private static final Random RANDOM = new Random();
    private static List<Item> ORE_CACHE = null;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        Level world = context.getWorld();

        if (!world.isClientSide && world instanceof ServerLevel serverLevel) {
            BlockPos pos = context.getPos();

            if (ORE_CACHE == null) {
                initializeOreCache();
            }

            if (!ORE_CACHE.isEmpty()) {
                Item randomOre = ORE_CACHE.get(RANDOM.nextInt(ORE_CACHE.size()));
                ItemStack dropStack = new ItemStack(randomOre);

                ItemEntity entity = new ItemEntity(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        dropStack);
                entity.setDefaultPickUpDelay();
                world.addFreshEntity(entity);
            }
        }
    }

    private void initializeOreCache() {
        TagKey<Block> oreTag = ForgeRegistries.BLOCKS.tags().createTagKey(
                new net.minecraft.resources.ResourceLocation("forge", "ores")
        );

        ORE_CACHE = ForgeRegistries.BLOCKS.getValues().stream()
                .filter(block -> ForgeRegistries.BLOCKS.tags().getTag(oreTag).contains(block))
                .map(Block::asItem)
                .filter(item -> item != net.minecraft.world.item.Items.AIR) // 排除没有物品形态的方块
                .distinct()
                .collect(Collectors.toList());
    }
}
