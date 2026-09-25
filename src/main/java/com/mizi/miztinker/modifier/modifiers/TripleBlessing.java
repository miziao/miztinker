package com.mizi.miztinker.modifier.modifiers;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TripleBlessing extends Modifier implements ToolStatsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        int upgradeCount = context.getUpgrades().getModifiers().size();

        float multiplier;
        if (upgradeCount > 0 && upgradeCount % 3 == 0) {
            multiplier = 0.6f;
        } else {
            multiplier = 0.3f * upgradeCount;
        }

        ToolStats.DURABILITY.multiply(builder, 1 + multiplier);
        ToolStats.ATTACK_DAMAGE.multiply(builder, 1 + multiplier);
        ToolStats.ATTACK_SPEED.multiply(builder, 1 + multiplier);
        ToolStats.MINING_SPEED.multiply(builder, 1 + multiplier);
        ToolStats.ARMOR.multiply(builder, 1 + multiplier);
        ToolStats.ARMOR_TOUGHNESS.multiply(builder, 1 + multiplier);

        ToolStats.DRAW_SPEED.multiply(builder, 1 + multiplier);
        ToolStats.VELOCITY.multiply(builder, 1 + multiplier);
        ToolStats.PROJECTILE_DAMAGE.multiply(builder, 1 + multiplier);
    }
}