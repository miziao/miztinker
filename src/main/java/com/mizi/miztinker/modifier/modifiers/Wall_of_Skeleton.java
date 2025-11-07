package com.mizi.miztinker.modifier.modifiers;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
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

public class Wall_of_Skeleton extends NoLevelsModifier implements InventoryTickModifierHook {


    /** 更新间隔（防止每 tick 都反复应用） */
    private static final int CHECK_INTERVAL = 10;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        // 注册 LivingTick 钩子，在玩家每 tick 检查
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {
        // ✅ 只在服务端执行逻辑
        if (level.isClientSide || !isCorrectSlot) return;
        // ✅ 确保是玩家
        if (!(holder instanceof Player player)) return;

        // 每 10 tick 检查一次
        if (player.tickCount % CHECK_INTERVAL != 0) return;

        // 获取魔力数据（Iron's Spellbooks API）
        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData == null) return;

        float currentMana = magicData.getMana();
        float maxMana = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA.get());

        // 如果魔力未满，给予抗性提升 V
        if (currentMana < maxMana) {
            MobEffectInstance resistance = new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    40, // 持续 2 秒（每 10 tick 重新刷新）
                    4,  // 等级 5（从 0 开始计数）
                    true, false, true
            );
            player.addEffect(resistance);
        }
    }
}
