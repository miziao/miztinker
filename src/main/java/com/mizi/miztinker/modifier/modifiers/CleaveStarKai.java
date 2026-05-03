package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.RequirementsModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.isFromDummmmmmyMod;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.modifierSeverance;

public class CleaveStarKai extends NoLevelsModifier implements
        MeleeHitModifierHook,
        RequirementsModifierHook,
        ValidateModifierHook {

    private static final ModifierId INSATIABLE_ID = new ModifierId("tconstruct", "insatiable");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.REQUIREMENTS, ModifierHooks.VALIDATE);
    }

    @Override
    public float beforeMeleeHit(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float damage, float baseKnockback, float knockback) {
        processCleave(tool, context);
        return knockback;
    }

    @Override
    public void failedMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageAttempted) {
        processCleave(tool, context);
    }

    private void processCleave(IToolStackView tool, ToolAttackContext context) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();

        if (target != null && player != null && target.getHealth() > 0) {
            if (isFromDummmmmmyMod(target)) return;
            float toolDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);

            modifierSeverance(target, player, toolDamage, 0.01f, 0f);
        }
    }


    @Override
    public @Nullable Component validate(IToolStackView tool, ModifierEntry entry) {
        if (tool.getModifierLevel(INSATIABLE_ID) > 0) {
            return null;
        }
        return requirementsError(entry);
    }

    @Override
    public Component requirementsError(ModifierEntry entry) {
        return Component.translatable("modifier.miztinker.cleave_star_kai.requirements");
    }
}