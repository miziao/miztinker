package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * 神话助力
 * 在下界时持续获得速度 II 效果
 */
public class MythicalPushArmor extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, net.minecraft.world.item.ItemStack stack) {

        if (!isCorrectSlot) return; // 确保装备在正确位置（盔甲槽中）
        if (holder == null) return;

        // 仅在下界生效
        if (world.dimension().location().equals(new ResourceLocation("minecraft:the_nether"))) {
            holder.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    40,  // 2秒持续，每tick刷新
                    1,   // 等级1 => 速度II
                    false,
                    false,
                    true
            ));
        } else {
            // 离开下界时移除速度效果
            holder.removeEffect(MobEffects.MOVEMENT_SPEED);
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}