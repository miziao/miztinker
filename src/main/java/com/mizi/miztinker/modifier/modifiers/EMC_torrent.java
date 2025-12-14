package com.mizi.miztinker.modifier.modifiers;

import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.math.BigInteger;

public class EMC_torrent extends Modifier
        implements MeleeDamageModifierHook, MeleeHitModifierHook {

    private static final float EMC_RATE = 6.0f;

    /** 本次攻击的最终理论伤害（包含所有修饰） */
    private float cachedFinalDamage = 0;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    /**
     * ★ 核心：让 EMC_torrent 的伤害计算最后执行
     */
    @Override
    public int getPriority() {
        return 1000; // 比 SoulEat、附魔、倍率都晚
    }

    /**
     * ① 伤害计算阶段（最终值）
     */
    @Override
    public float getMeleeDamage(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float baseDamage,
            float damage
    ) {
        this.cachedFinalDamage = damage; // ← 已包含噬魂、附魔、效果等
        return damage;
    }

    /**
     * ② 命中后结算 EMC
     */
    @Override
    public void afterMeleeHit(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float damageDealt
    ) {
        if (!(context.getAttacker() instanceof ServerPlayer player)) return;
        if (cachedFinalDamage <= 0) return;

        long emcToAdd = (long) (cachedFinalDamage * EMC_RATE);
        cachedFinalDamage = 0; // 防止连锁触发

        player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(knowledge -> {
            knowledge.setEmc(
                    knowledge.getEmc().add(BigInteger.valueOf(emcToAdd))
            );
            knowledge.syncEmc(player);
        });
    }
}