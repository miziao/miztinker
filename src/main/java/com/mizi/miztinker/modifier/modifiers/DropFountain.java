package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import static com.mizi.miztinker.modifier.register.MiztinkerEffect.DropFountainEffect;

public class DropFountain extends NoLevelsModifier implements MeleeHitModifierHook {

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (damageDealt <= 0) return;

        LivingEntity target = context.getLivingTarget();
        if (target != null && target.isAlive()) {
            int level = modifier.getLevel();
            int duration = Integer.MAX_VALUE; // 无限时长

            // 给生物添加 DropFountainEffect 药水效果，玩家不受影响
            target.addEffect(new MobEffectInstance(
                    DropFountainEffect.get(),
                    duration,
                    0,      // 效果等级
                    false,  // 不显示图标
                    false,  // 不显示粒子
                    true    // 仅客户端可见（选项，可按需求）
            ));
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, slimeknights.tconstruct.library.modifiers.ModifierHooks.MELEE_HIT);
    }
}