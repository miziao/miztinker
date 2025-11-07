package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class EnchantedGold extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final int MAX_ABSORB_TIME = 2400;  // 2 minutes
    private static final int MAX_HEAL_TIME = 400;     // 20 seconds
    private static final int MAX_FIRE_TIME = 6000;    // 5 minutes
    private static final int MAX_RESISTANCE_TIME = 6000; // 5 minutes

    // 记录每个玩家上一次的饥饿值
    private final Map<UUID, Integer> prevFoodLevels = new WeakHashMap<>();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(holder instanceof Player player)) return;
        if (!isCorrectSlot) return; // 只在正确槽位

        UUID playerId = player.getUUID();
        int currentFood = player.getFoodData().getFoodLevel();
        int prevFood = prevFoodLevels.getOrDefault(playerId, currentFood);

        if (currentFood > prevFood) {
            // 实际增加的饥饿值
            int hungerGained = currentFood - prevFood;
            if (hungerGained > 0) {
                // 激活附魔金苹果效果
                applyPotionEffects(player);
            }
        }

        // 更新记录
        prevFoodLevels.put(playerId, currentFood);
    }

    private void applyPotionEffects(Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, MAX_ABSORB_TIME, 3, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, MAX_HEAL_TIME, 1, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MAX_FIRE_TIME, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, MAX_RESISTANCE_TIME, 0, false, false, true));
    }
}