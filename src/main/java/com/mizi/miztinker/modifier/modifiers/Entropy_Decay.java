package com.mizi.miztinker.modifier.modifiers;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class Entropy_Decay extends NoLevelsModifier implements MeleeHitModifierHook, ToolStatsModifierHook {

    private static final ResourceLocation DAMAGE_KEY = new ResourceLocation("miztinker", "entropy_decay_damage");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();
        Level level = context.getAttacker().level();

        if (level.isClientSide || player == null || target == null || target.isAlive()) {
            return;
        }

        ModDataNBT data = tool.getPersistentData();
        float currentDamage = data.getFloat(DAMAGE_KEY);

        // 初始值取自工具面板
        if (currentDamage <= 0f || Float.isNaN(currentDamage)) {
            currentDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);
        }

        // 每次击杀降低攻击力 0.1%
        float reduction = target.getMaxHealth() * 0.001f;
        currentDamage -= reduction;

        // 若降至 0 以下，则进入熵崩坏态（无穷大）
        if (currentDamage <= 0f) {
            currentDamage = Float.POSITIVE_INFINITY;
        }

        data.putFloat(DAMAGE_KEY, currentDamage);

        // 强制 rebuild stats
        ItemStack stack = player.getMainHandItem(); // 攻击者主手
        ToolStack.from(stack).rebuildStats();
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        ModDataNBT data = (ModDataNBT) context.getPersistentData();
        if (data.contains(DAMAGE_KEY)) {
            float value = data.getFloat(DAMAGE_KEY);

            // 无限攻击力显示为 1/0
            ToolStats.ATTACK_DAMAGE.update(builder, Float.isInfinite(value) ? 1f / 0f : value);
        }
    }
}
