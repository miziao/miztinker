package com.mizi.miztinker.modifier.modifiers;

import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.mizi.miztinker.miztinker.getResource;

public class EMC_torrent extends NoLevelsModifier implements MeleeDamageModifierHook, MeleeHitModifierHook {

    public static final ResourceLocation EMC_TORRENT_ID = getResource("emc_torrent");

    private static final float EMC_RATE = 6.0f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
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
        if (!(context.getAttacker() instanceof ServerPlayer player)) {
            return damage;
        }

        float theoreticalDamage = estimateTheoreticalDamage(context, baseDamage, damage);

        EmcTorrentDamageTracker.cacheTheoreticalDamage(player, theoreticalDamage);

        return damage;
    }

    @Override
    public void afterMeleeHit(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float damageDealt
    ) {
        if (!(context.getAttacker() instanceof ServerPlayer player)) {
            return;
        }

        float cachedTheoreticalDamage = EmcTorrentDamageTracker.popTheoreticalDamage(player);
        float eventFinalDamage = EmcTorrentDamageTracker.popFinalDamage(player);

        float finalDamage = Math.max(
                Math.max(eventFinalDamage, cachedTheoreticalDamage),
                damageDealt
        );

        if (finalDamage > 0.0f) {
            addEmc(player, finalDamage);
        }
    }

    private static float estimateTheoreticalDamage(
            ToolAttackContext context,
            float baseDamage,
            float damage
    ) {
        float result = Math.max(0.0f, damage);

        float cooldown = context.getCooldown();
        if (cooldown < 1.0f) {
            result *= 0.2f + cooldown * cooldown * 0.8f;
        }

        return Math.max(0.0f, result);
    }

    private static void addEmc(ServerPlayer player, float damage) {
        if (damage <= 0.0f || Float.isNaN(damage) || Float.isInfinite(damage)) {
            return;
        }

        BigInteger emcToAdd = BigDecimal.valueOf((double) damage)
                .multiply(BigDecimal.valueOf((double) EMC_RATE))
                .toBigInteger();

        if (emcToAdd.signum() <= 0) {
            return;
        }

        player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(knowledge -> {
            BigInteger currentEmc = knowledge.getEmc();
            knowledge.setEmc(currentEmc.add(emcToAdd));
            knowledge.syncEmc(player);
        });
    }
}