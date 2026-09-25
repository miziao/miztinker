package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LifeStealing extends Modifier implements MeleeDamageModifierHook, MeleeHitModifierHook, TooltipModifierHook {

    private static final float HEAL_RATE_PER_LEVEL = 0.10f;

    private static final Map<UUID, DamageRecord> THEORETICAL_DAMAGE_CACHE = new ConcurrentHashMap<>();

    private static class DamageRecord {
        final float damage;
        final long gameTime;

        DamageRecord(float damage, long gameTime) {
            this.damage = damage;
            this.gameTime = gameTime;
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public float getMeleeDamage(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float baseDamage,
            float damage
    ) {
        LivingEntity attacker = context.getAttacker();

        if (attacker.level().isClientSide) {
            return damage;
        }

        if (context.getLivingTarget() == null) {
            return damage;
        }

        float theoreticalDamage = sanitizeDamage(damage);

        if (theoreticalDamage > 0.0f) {
            THEORETICAL_DAMAGE_CACHE.put(
                    attacker.getUUID(),
                    new DamageRecord(theoreticalDamage, attacker.level().getGameTime())
            );
        }

        return damage;
    }

    @Override
    public void afterMeleeHit(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float damageDealt
    ) {
        LivingEntity attacker = context.getAttacker();

        if (attacker.level().isClientSide) {
            return;
        }

        if (attacker.isDeadOrDying()) {
            return;
        }

        int level = modifier.getLevel();

        if (level <= 0) {
            return;
        }

        float theoreticalDamage = popTheoreticalDamage(attacker);

        float finalDamage = Math.max(
                sanitizeDamage(damageDealt),
                sanitizeDamage(theoreticalDamage)
        );

        if (finalDamage <= 0.0f) {
            return;
        }

        float healAmount = finalDamage * HEAL_RATE_PER_LEVEL * level;
        healAmount = sanitizeDamage(healAmount);

        if (healAmount <= 0.0f) {
            return;
        }

        attacker.heal(healAmount);
    }

    private static float popTheoreticalDamage(LivingEntity attacker) {
        if (attacker == null) {
            return 0.0f;
        }

        DamageRecord record = THEORETICAL_DAMAGE_CACHE.remove(attacker.getUUID());

        if (record == null) {
            return 0.0f;
        }

        long now = attacker.level().getGameTime();

        if (Math.abs(now - record.gameTime) > 1) {
            return 0.0f;
        }

        return sanitizeDamage(record.damage);
    }

    private static float sanitizeDamage(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return 0.0f;
        }

        return Math.max(value, 0.0f);
    }

    @Override
    public void addTooltip(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @Nullable Player player,
            @NotNull List<Component> tooltip,
            @NotNull TooltipKey tooltipKey,
            @NotNull TooltipFlag tooltipFlag
    ) {
        int level = modifier.getLevel();

        if (level <= 0) {
            return;
        }

        int percent = Math.round(HEAL_RATE_PER_LEVEL * level * 100.0f);

        MutableComponent text = Component.translatable(
                "modifier.miztinker.life_stealing.tooltip",
                percent
        );

        tooltip.add(text);
    }
}