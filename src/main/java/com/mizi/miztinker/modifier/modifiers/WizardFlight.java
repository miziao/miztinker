package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class WizardFlight extends NoLevelsModifier implements InventoryTickModifierHook {

    /** 每 2 秒（40 tick）消耗 1 点耐久 */
    private static final int DURABILITY_INTERVAL = 20;
    private static final int DURABILITY_LOSS = 1;

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !(holder instanceof Player player)) return;

        boolean active = isSelected && itemSlot == player.getInventory().selected;

        // 工具损坏时，直接关闭飞行并退出
        if (tool.isBroken()) {
            disableFlight(player);
            return;
        }

        if (active) {
            // ✅ 启用飞行
            enableFlight(player);

            // ✅ 每 1 秒扣 1 点耐久
            if (level.getGameTime() % DURABILITY_INTERVAL == 0) {
                ToolDamageUtil.damage(tool, DURABILITY_LOSS, player, stack);

                // 若刚好在本次消耗中被打断（损坏）
                if (tool.isBroken()) {
                    disableFlight(player);
                }
            }
        } else {
            // ❌ 不在主手则禁飞
            disableFlight(player);
        }
    }

    /** 启用飞行（区分服务端与客户端） */
    private void enableFlight(Player player) {
        if (player instanceof ServerPlayer sp) {
            sp.getAbilities().mayfly = true;
            sp.onUpdateAbilities();
        } else {
            player.getAbilities().mayfly = true;
        }
    }

    /** 禁用飞行（保留创造/旁观玩家） */
    private void disableFlight(Player player) {
        if (player instanceof ServerPlayer sp) {
            GameType gm = sp.gameMode.getGameModeForPlayer();
            if (gm != GameType.CREATIVE && gm != GameType.SPECTATOR) {
                sp.getAbilities().mayfly = false;
                sp.getAbilities().flying = false;
                sp.onUpdateAbilities();
            }
        } else if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}