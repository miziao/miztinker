package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.*;
import slimeknights.tconstruct.library.modifiers.hook.behavior.AttributesModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * 灵魂汲取（Soul Drain）
 * 每级 +0.5 最大生命值, +0.1 生命回复速度
 */
public class SoulDrain extends Modifier implements AttributesModifierHook, InventoryTickModifierHook {

    private static final UUID HEALTH_UUID = UUID.fromString("c2f7b1be-2c5a-4f13-8a8c-5bfe16a35d11");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ATTRIBUTES);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    /** 每级增加生命上限和生命恢复速度 */
    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry entry, EquipmentSlot slot,
                              BiConsumer<Attribute, AttributeModifier> consumer) {
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            int level = entry.getLevel();

            // 每级 +0.5 生命上限
            consumer.accept(Attributes.MAX_HEALTH, new AttributeModifier(
                    HEALTH_UUID,
                    "soul_drain_health_bonus",
                    level * 0.5f,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }

    /** 每秒回复生命（Forge 无再生属性，这里用 tick 处理） */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && holder.isAlive()) {
            // 每秒执行一次
            if (holder.tickCount % 20 == 0) {
                int level = entry.getLevel();
                float healAmount = 0.1f * level; // 每级每秒 +0.1HP
                if (holder.getHealth() < holder.getMaxHealth()) {
                    holder.heal(healAmount);
                }
            }
        }
    }
}
