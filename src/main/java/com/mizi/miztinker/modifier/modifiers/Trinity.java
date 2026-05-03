package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.util.C;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

public class Trinity extends Modifier implements ToolStatsModifierHook, TooltipModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS, ModifierHooks.TOOLTIP);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        int level = modifier.getLevel();
        float multiplier = (level == 3) ? 0.6f : 0.3f;
        float bonus = 1 + (multiplier * level);

        ToolStats.DRAW_SPEED.multiply(builder, bonus);
        ToolStats.MINING_SPEED.multiply(builder, bonus);
        ToolStats.DURABILITY.multiply(builder, bonus);
        ToolStats.ATTACK_SPEED.multiply(builder, bonus);
        ToolStats.ATTACK_DAMAGE.multiply(builder, bonus);
        ToolStats.VELOCITY.multiply(builder, bonus);
        ToolStats.ACCURACY.multiply(builder, bonus);
        ToolStats.PROJECTILE_DAMAGE.multiply(builder, bonus);
        ToolStats.ARMOR.multiply(builder, bonus);
        ToolStats.ARMOR_TOUGHNESS.multiply(builder, bonus);
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        if (player != null && modifier.getLevel() == 3) {
            String translatedText = Component.translatable("modifier.miztinker.trinity.peak_warning").getString();
            tooltip.add(C.getRainbowComponent(translatedText));
        }
    }
}