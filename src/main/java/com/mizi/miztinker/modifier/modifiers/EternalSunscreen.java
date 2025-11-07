package com.mizi.miztinker.modifier.modifiers;

import de.teamlapen.vampirism.core.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class EternalSunscreen extends NoLevelsModifier implements InventoryTickModifierHook {



    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, net.minecraft.world.item.ItemStack stack) {
        if (!isCorrectSlot) return;

        // 永久刷新防晒霜效果
        holder.addEffect(new MobEffectInstance(
                ModEffects.SUNSCREEN.get(),
                40,   // 2秒持续时间，每tick刷新，保证效果不断
                4,    // 放大等级（4=完全免疫阳光伤害）
                false, // 不隐藏粒子
                true   // 显示图标
        ));
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}