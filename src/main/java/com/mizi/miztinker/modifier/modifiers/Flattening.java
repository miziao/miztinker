package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Iterator;
import java.util.List;

public class Flattening extends NoLevelsModifier implements ProcessLootModifierHook {

    /** 按经验等级计算倍数：100级 → 2倍，200级 → 3倍 */
    private int getMultiplier(Player player) {
        if (player == null) return 1;
        return 1 + player.experienceLevel / 100;
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> list, LootContext context) {

        // 获取玩家（参考 GainsAlone 的写法）
        Player player = null;

        Entity killer = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (killer instanceof Player p) player = p;

        if (player == null) {
            Entity thisEntity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (thisEntity instanceof Player p2) player = p2;
        }

        int multiplier = getMultiplier(player);
        if (multiplier <= 1) return;

        // ----------- 核心：直接 setCount，不增加掉落数量（不卡） -----------
        for (ItemStack stack : list) {
            if (!stack.isEmpty()) {
                stack.setCount(stack.getCount() * multiplier);
            }
        }
        // --------------------------------------------------------------
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.PROCESS_LOOT);
    }
}