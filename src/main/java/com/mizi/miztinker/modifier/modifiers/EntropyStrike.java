package com.mizi.miztinker.modifier.modifiers;


import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class EntropyStrike extends NoLevelsModifier implements MeleeDamageModifierHook {

    @Override
    public int getPriority() {
        return 100; // 高优先级，确保在基础伤害计算后额外加面板伤害
    }

    @Override
    public float getMeleeDamage(IToolStackView tool,
                                ModifierEntry entry,
                                ToolAttackContext context,
                                float baseDamage,
                                float damage) {

        LivingEntity target = context.getLivingTarget();
        if (target == null) return damage;

        // 获取武器面板伤害
        float panelDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);

        // 如果面板伤害为无限，改为 Float.MAX_VALUE
        if (Float.isInfinite(panelDamage)) {
            return damage + Float.MAX_VALUE;
        }

        // 正常情况：额外造成一次等额面板伤害
        return damage + panelDamage;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }
}