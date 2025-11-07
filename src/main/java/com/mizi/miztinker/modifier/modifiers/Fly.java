package com.mizi.miztinker.modifier.modifiers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;


public class Fly extends NoLevelsModifier implements InventoryTickModifierHook, EquipmentChangeModifierHook {
    @Override
    public void onEquip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;

        // 直接开启飞行能力，无需等级判断
        player.getAbilities().mayfly = true;
    }

    @Override
    public void onUnequip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;

        // 如果替换物仍然有该 modifier，则不关闭飞行
        IToolStackView replacement = context.getReplacementTool();
        if (replacement != null && replacement.getModifierLevel(this) > 0) {
            return;
        }

        // 保护创意/旁观模式玩家
        GameType gm = player.gameMode.getGameModeForPlayer();
        if (gm == GameType.SPECTATOR || gm == GameType.CREATIVE) return;

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(holder instanceof Player player)) return;
        if (!isCorrectSlot) return; // 只在正确槽位

        // 只要穿上就可以飞，server 上同步
        if (player instanceof ServerPlayer sp) {
            sp.getAbilities().mayfly = true;
        } else {
            player.getAbilities().mayfly = true;
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}
