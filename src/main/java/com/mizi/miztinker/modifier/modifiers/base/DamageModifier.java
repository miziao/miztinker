package com.mizi.miztinker.modifier.modifiers.base;

import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurt;

@Getter
public class DamageModifier extends NoLevelsModifier implements MeleeHitModifierHook {

    private final float baseDamage; // 固定伤害
    private final float percent;    // 百分比伤害

    public DamageModifier(float baseDamage, float percent) {
        this.baseDamage = baseDamage;
        this.percent = percent;
    }

    // 注册 MELEE_HIT 钩子
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    // 在攻击命中前触发，保证伤害生效
    @Override
    public float beforeMeleeHit(IToolStackView tool, ModifierEntry modifier,
                                ToolAttackContext context, float damage,
                                float baseKnockback, float knockback) {

        LivingEntity target = context.getLivingTarget();
        Player attacker = context.getPlayerAttacker();

        if (target == null || attacker == null) return knockback;

        double attackValue = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float totalDamage = baseDamage + (float)(attackValue * percent);

        // 1. 获取玩家伤害来源（1.20+ 正确写法）
        DamageSource source = attacker.damageSources().playerAttack(attacker);

        // 2. 调用自定义伤害
        forceHurt(target, source, totalDamage);

        return knockback;
    }

    // 失败攻击也触发（可选）
    @Override
    public void failedMeleeHit(IToolStackView tool, ModifierEntry modifier,
                               ToolAttackContext context, float damageAttempted) {

        LivingEntity target = context.getLivingTarget();
        Player attacker = context.getPlayerAttacker();

        if (target == null || attacker == null) return;

        double attackValue = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float totalDamage = baseDamage + (float)(attackValue * percent);

        // ✅ 1.20+ 正确写法
        DamageSource source = attacker.damageSources().playerAttack(attacker);

        // 调用自定义伤害
        forceHurt(target, source, totalDamage);
    }
}