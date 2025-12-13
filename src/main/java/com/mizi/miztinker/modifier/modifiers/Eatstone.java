package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.RemoveBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Eatstone extends NoLevelsModifier
        implements RemoveBlockModifierHook {

    /* ---------- Forge 标签 ---------- */

    private static final TagKey<Block> FORGE_STONE =
            TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "stone"));

    private static final TagKey<Block> FORGE_ORES =
            TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores"));

    /* ---------- 食物参数 ---------- */

    private static final int FOOD_STONE = 1;
    private static final float SAT_STONE = 0.1f;

    private static final int FOOD_ORE = 3;
    private static final float SAT_ORE = 0.3f;

    /* ---------- Hook 注册 ---------- */

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.REMOVE_BLOCK);
    }

    @Override
    public Boolean removeBlock(IToolStackView tool, ModifierEntry modifier,
                               ToolHarvestContext context) {

        Player player = context.getPlayer();
        if (player == null || !context.isEffective()) {
            return null;
        }

        BlockState state = context.getState();

        int food = 0;
        float saturation = 0f;

        if (state.is(FORGE_STONE)) {
            food = FOOD_STONE;
            saturation = SAT_STONE;
        } else if (state.is(FORGE_ORES)) {
            food = FOOD_ORE;
            saturation = SAT_ORE;
        } else {
            return null;
        }

        Level level = context.getWorld();

        // ✅ 移除方块（不掉落）
        level.removeBlock(context.getPos(), false);

        // ✅ 直接吃掉石头（这里是关键）
        if (!level.isClientSide) {
            player.getFoodData().eat(food, saturation);

            level.playSound(null, context.getPos(),
                    SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, context.getPos(),
                    SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 1.0F);
        }

        // ❗阻止 playerDestroy → 无掉落
        return false;
    }
}
