package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;


public class SteelHowForged extends Modifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    /**
     * 每 tick 检查盔甲栏上的所有等级
     */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !isCorrectSlot) return;

        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armor = holder.getItemBySlot(slot);
                if (armor.isEmpty()) continue;

                ToolStack toolArmor = ToolStack.from(armor);
                if (toolArmor == null) continue; // 保守检查
                total += toolArmor.getModifierLevel(this);
            }
        }

        if (total > 0) {
            int amplifier = Math.max(0, total - 1);
            // 210 tick 保证效果平滑刷新（每 20 tick 刷新一次也不会闪烁）
            holder.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 210, amplifier, false, false, true));
        }
    }

}