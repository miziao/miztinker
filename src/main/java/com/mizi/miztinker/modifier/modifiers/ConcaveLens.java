package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;
import java.util.List;

public class ConcaveLens extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide || tool.isBroken()) return;

        if (!isCorrectSlot) return;

        Collection<MobEffectInstance> activeEffects = holder.getActiveEffects();
        List<MobEffectInstance> debuffs = activeEffects.stream()
                .filter(effect -> !effect.getEffect().isBeneficial())
                .toList();

        if (debuffs.isEmpty()) return;

        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class,
                holder.getBoundingBox().inflate(5.0D),
                entity -> entity != holder && entity.isAlive());

        if (nearbyEntities.isEmpty()) return;

        LivingEntity target = nearbyEntities.get(world.random.nextInt(nearbyEntities.size()));

        for (MobEffectInstance effect : debuffs) {
            int finalAmplifier = effect.getAmplifier();
            int finalDuration = effect.getDuration();

            MobEffectInstance targetEffect = target.getEffect(effect.getEffect());
            if (targetEffect != null) {
                finalAmplifier = Math.min(targetEffect.getAmplifier() + effect.getAmplifier() + 1, 254);
                finalDuration = Math.max(targetEffect.getDuration(), effect.getDuration());
            }

            target.addEffect(new MobEffectInstance(
                    effect.getEffect(),
                    finalDuration,
                    finalAmplifier,
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()
            ));

            holder.removeEffect(effect.getEffect());
        }
    }
}