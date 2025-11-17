package com.mizi.miztinker.modifier.modifiers;

import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.math.BigInteger;

public class EMC_torrent extends NoLevelsModifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier,
                              @NotNull ToolAttackContext context, float damageDealt) {
        // 攻击者必须是玩家
        if (!(context.getAttacker() instanceof Player player)) return;
        // 只在服务端执行
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // 目标必须是生物
        Entity target = context.getTarget();
        if (!(target instanceof LivingEntity)) return;

        // 只处理造成了实际伤害的情况
        if (damageDealt <= 0) return;

        // 伤害的 50% 转化为 EMC
        long emcToAdd = (long) Math.floor(damageDealt * 6.0);
        if (emcToAdd <= 0) return;

        // 从 ProjectE 的 KNOWLEDGE_CAPABILITY 获取并增加 EMC
        player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(knowledge -> {
            BigInteger current = knowledge.getEmc();
            BigInteger add = BigInteger.valueOf(emcToAdd);
            BigInteger newTotal = current.add(add);
            knowledge.setEmc(newTotal);
            knowledge.syncEmc(serverPlayer);
        });
    }
}