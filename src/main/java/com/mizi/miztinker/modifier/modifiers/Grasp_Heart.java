package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.common.TinkerDamageTypes;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Grasp_Heart extends NoLevelsModifier implements MeleeHitModifierHook {


    /** 每级基础触发概率 0.1% */
    private final double baseCritChance = 0.01;
    /** 每个玩家经验等级增加的概率 0.01% */
    private final double perXpLevelChance = 0.001;
    /** 致命一击伤害 */
    private final float damageAmount = 100000f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        // 注册近战命中钩子
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float damageDealt) {
        LivingEntity attacker = context.getAttacker();
        LivingEntity target = context.getLivingTarget();
        if (target == null || target.level().isClientSide || !target.isAlive()) return;


        // 根据 modifier 等级计算基础概率
        int level = entry.getLevel();
        double chance = baseCritChance * level;

        // 如果攻击者是玩家，增加基于玩家经验等级的概率
        if (attacker instanceof Player player) {
            int xpLevel = player.experienceLevel;
            chance += perXpLevelChance * xpLevel;
        }

        // 概率最大为 1（100%）
        if (chance > 1.0) chance = 1.0;

        // 判断是否触发致命一击
        if (RANDOM.nextDouble() < chance) {
            DamageSource source = TinkerDamageTypes.source(
                    target.level().registryAccess(),
                    DamageTypes.GENERIC,
                    context.getPlayerAttacker()
            );

            // 使用 secondary damage，保证免疫期逻辑正确
            ToolAttackUtil.attackEntitySecondary(source, damageAmount, target, target, true);

            // 在服务器端显示粒子
            if (target.level() instanceof ServerLevel serverLevel) {
                Vec3 pos = target.position();
                serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        pos.x, pos.y + 1, pos.z,
                        5, 0.5, 0.5, 0.5, 0.0
                );
            }

            // 给玩家客户端提示
            if (context.getPlayerAttacker() instanceof Player) { Player player = (Player) context.getPlayerAttacker();
                player.displayClientMessage( Component.literal("§d⚡ 第八位阶魔法 心脏掌握发动!"), true );
            }
        }
    }
}
