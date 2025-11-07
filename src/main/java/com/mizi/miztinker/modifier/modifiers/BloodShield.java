package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;

public class BloodShield extends NoLevelsModifier implements DamageBlockModifierHook {



    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry, EquipmentContext context,
                                   EquipmentSlot slot, DamageSource source, float damage) {
        // 首先检查伤害是否小于等于30，如果是直接格挡
        if (damage <= 30) {
            return true;
        }

        // 确保实体是服务器玩家
        if (!(context.getEntity() instanceof ServerPlayer player)) {
            return false;
        }

        // 获取吸血鬼玩家数据
        VampirePlayer vampirePlayer = VampirePlayer.get(player);
        if (vampirePlayer == null || vampirePlayer.getLevel() <= 0) {
            return false;
        }

        int currentBlood = vampirePlayer.getBloodLevel();
        int bloodCost = (int) Math.ceil(damage / 200.0); // 根据伤害计算血量消耗

        // 如果血量足够，消耗血液格挡
        if (currentBlood >= bloodCost) {
            if (vampirePlayer.useBlood(bloodCost, false)) {
                player.sendSystemMessage(Component.literal("护盾消耗了血液: " + bloodCost));
                return true; // 完全格挡
            }
        }

        return false; // 无法格挡
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}