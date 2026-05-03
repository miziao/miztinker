package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * IgnisterField - 最终可用版本
 * 功能：
 * 1. 缓存玩家已完成进度数量
 * 2. 近战命中时按缓存触发若干次随机类型额外伤害
 * 3. 每种伤害只显示一次
 * 4. 显示 tooltip
 */
public class IgnisterField extends NoLevelsModifier implements MeleeHitModifierHook, TooltipModifierHook {

    private static final ResourceLocation TAG_ADV_COUNT = ResourceLocation.fromNamespaceAndPath("miztinker", "ignister_advancements");
    private static final ResourceLocation TAG_LAST_UPDATE = ResourceLocation.fromNamespaceAndPath("miztinker", "ignister_last_update");
    private static final int UPDATE_INTERVAL = 1200; // tick
    private static final Random RANDOM = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!(context.getAttacker() instanceof ServerPlayer player)) return;

        LivingEntity target = context.getLivingTarget();
        if (target == null || !target.isAlive()) return;

        ModDataNBT data = tool.getPersistentData();
        int currentTime = (int) player.level().getGameTime();
        int lastUpdate = data.getInt(TAG_LAST_UPDATE);
        int advCount = data.getInt(TAG_ADV_COUNT);

        // 周期性刷新进度缓存
        if (currentTime - lastUpdate > UPDATE_INTERVAL) {
            int newCount = 0;
            try {
                if (player.server != null) {
                    for (Advancement adv : player.server.getAdvancements().getAllAdvancements()) {
                        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(adv);
                        if (progress.isDone()) newCount++;
                    }
                }
            } catch (Exception ignored) {
                newCount = advCount;
            }
            advCount = newCount;
            data.putInt(TAG_ADV_COUNT, advCount);
            data.putInt(TAG_LAST_UPDATE, currentTime);
        }

        int bonusCount = advCount / 6;
        if (bonusCount <= 0) return;

        float baseDamage = Math.max(1f, damageDealt);

        // 用 Set 去重每种触发的伤害类型
        Set<String> triggeredNames = new HashSet<>();

        for (int i = 0; i < bonusCount; i++) {
            String damageName = applyRandomExtraDamage(player, target, baseDamage);
            if (damageName != null) triggeredNames.add(damageName);
        }

        // 一次性显示所有触发的伤害类型
        if (!triggeredNames.isEmpty()) {
            String joinedNames = String.join("、", triggeredNames);
            player.displayClientMessage(
                    Component.literal("触发了 " + joinedNames + "§7@火灵天星支援"),
                    true
            );
        }
    }

    /**
     * 随机造成额外伤害并返回名称（null 表示不提示）
     */
    private String applyRandomExtraDamage(ServerPlayer player, LivingEntity target, float baseDamage) {
        Level level = player.level();
        int type = RANDOM.nextInt(6);

        // 特效触发
        if (type == 0) target.setSecondsOnFire(2);
        else if (type == 3 && level instanceof ServerLevel serverLevel) {
            try { target.thunderHit(serverLevel, null); } catch (Throwable ignored) {}
        }

        DamageSource source;
        String damageName = null;
        switch (type) {
            case 0 -> { source = level.damageSources().inFire(); damageName = "§c热烫烫"; }
            case 1 -> { source = level.damageSources().inWall(); damageName = "§b凉飕飕"; }
            case 2 -> { source = level.damageSources().fall(); damageName = "§a风萧萧"; }
            case 3 -> { source = level.damageSources().lightningBolt(); damageName = "§e闪亮亮"; }
            case 4 -> { source = level.damageSources().fellOutOfWorld(); damageName = "§0郁闷闷"; }
            case 5 -> { source = level.damageSources().anvil(player); damageName = "§6沉咚咚"; }
            default -> source = level.damageSources().generic(); // 不提示
        }

        // 使用匠魂攻击辅助函数造成伤害（可保留工具面板加成）
        ToolAttackUtil.attackEntitySecondary(source, baseDamage, target, target, true);

        return damageName;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player,
                           java.util.List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        int advancementCount = data.getInt(TAG_ADV_COUNT);
        int bonusCount = advancementCount / 6;

        tooltip.add(Component.literal("§e当前已完成进度：§a" + advancementCount));
        tooltip.add(Component.literal("§b当前可额外触发：§a" + bonusCount + " 次火灵天星支援"));
    }
}