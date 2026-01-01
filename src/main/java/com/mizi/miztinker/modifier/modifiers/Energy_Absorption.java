package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.DurabilityManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class Energy_Absorption extends Modifier implements MeleeHitModifierHook, ToolStatsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        long bonus = DurabilityManager.getTheoreticalMax(context);

        if (bonus > 0) {
            ToolStats.DURABILITY.add(builder, (float) bonus);
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();
        Level level = context.getAttacker().level();

        if (level.isClientSide || player == null || target == null || target.isAlive()) {
            return;
        }

        long gain = (long) (target.getMaxHealth() * modifier.getLevel());
        long currentBonus = DurabilityManager.getTheoreticalMax(tool);

        DurabilityManager.setTheoreticalMax(tool, currentBonus + gain);

        if (tool instanceof ToolStack toolStack) {
            toolStack.rebuildStats();
        }

        player.displayClientMessage(
                Component.literal("§b[能量吸收] §f获得最大耐久加成: §a+" + gain),
                true
        );
    }
}