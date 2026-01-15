package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.*;

public class Elemental extends Modifier implements MeleeHitModifierHook, ProjectileHitModifierHook {
    private static List<MobEffect> BENEFICIAL_EFFECTS = null;
    private static final Random RANDOM = new Random();

    private static void ensureEffectsInitialized() {
        if (BENEFICIAL_EFFECTS == null) {
            BENEFICIAL_EFFECTS = new ArrayList<>();
            for (MobEffect effect : ForgeRegistries.MOB_EFFECTS) {
                var effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                if (effectId != null) {
                    if ((effectId.getNamespace().equals("minecraft") && effect.getCategory() == MobEffectCategory.BENEFICIAL)
                            || effect == MiztinkerEffect.StrengthOldEffect.get()) {
                        BENEFICIAL_EFFECTS.add(effect);
                    }
                }
            }
        }
    }

    public static MobEffect getRandomBeneficialEffect() {
        ensureEffectsInitialized();
        return BENEFICIAL_EFFECTS.get(RANDOM.nextInt(BENEFICIAL_EFFECTS.size()));
    }

    public static void applyRandomEffects(Player player, int count, int addDuration) {
        Set<MobEffect> chosenEffects = new HashSet<>();
        int actualCount = Math.min(count, BENEFICIAL_EFFECTS != null ? BENEFICIAL_EFFECTS.size() : 1);

        while (chosenEffects.size() < actualCount) {
            chosenEffects.add(getRandomBeneficialEffect());
        }

        for (MobEffect effect : chosenEffects) {
            int finalDuration = addDuration;
            int finalAmplifier = 0;

            MobEffectInstance existing = player.getEffect(effect);
            if (existing != null) {
                finalAmplifier = existing.getAmplifier() + 1;
                finalDuration = existing.getDuration() + addDuration;
            }

            player.addEffect(new MobEffectInstance(effect, finalDuration, finalAmplifier));
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float damageDealt) {
        Player player = context.getPlayerAttacker();
        if (context.getLivingTarget() != null && player != null) {
            processElementalEffect(player, entry.getLevel());
        }
    }

    @Override
    public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT data, ModifierEntry entry, Projectile projectile, EntityHitResult hit, LivingEntity shooter, LivingEntity target) {
        if (projectile instanceof AbstractArrow arrow && target != null && shooter instanceof Player player) {
            processElementalEffect(player, entry.getLevel());
            arrow.remove(Entity.RemovalReason.KILLED);
        }
        return false;
    }

    private void processElementalEffect(Player player, int level) {
        int count = level;

        int durationInTicks = (int) (Math.pow(3, level) * 20);

        applyRandomEffects(player, count, durationInTicks);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.PROJECTILE_HIT);
    }
}