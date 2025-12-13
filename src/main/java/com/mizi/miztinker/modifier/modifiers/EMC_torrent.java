package com.mizi.miztinker.modifier.modifiers;

import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.math.BigInteger;

public class EMC_torrent extends NoLevelsModifier
        implements MeleeDamageModifierHook, MeleeHitModifierHook {

    /** 用来存储本次攻击匠魂计算后的“理论伤害” */
    private float cachedDamage = 0;

    private static final float EMC_RATE = 6.0f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public float getMeleeDamage(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float baseDamage,
            float damage ) {

        this.cachedDamage = damage;   // 记录本次攻击的理论伤害
        return damage;                // 不修改匠魂本来的伤害
    }

    @Override
    public void afterMeleeHit(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float damageDealt ) {

        // 必须是服务器端玩家
        if (!(context.getAttacker() instanceof ServerPlayer player)) return;

        Entity target = context.getTarget();

        // 没有理论伤害则不发放
        if (cachedDamage <= 0) return;

        long emcToAdd = (long) (cachedDamage * EMC_RATE);
        cachedDamage = 0; // 清理缓存防止叠加

        // ProjectE：增加 EMC
        player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(knowledge -> {
            BigInteger total = knowledge.getEmc().add(BigInteger.valueOf(emcToAdd));
            knowledge.setEmc(total);
            knowledge.syncEmc(player);
        });
    }
}