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

import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;

import java.util.Optional;

public class Blood_Wing extends NoLevelsModifier implements InventoryTickModifierHook, EquipmentChangeModifierHook {


    /** 当装备到槽位时（例如穿上）——只在 server 的 ServerPlayer 上开启飞行（若满足吸血鬼等级条件） */


    @Override
    public void onEquip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;
        Optional<VampirePlayer> opt = VampirePlayer.getOpt(player).resolve();
        if (opt.isPresent() && opt.get().getLevel() >= 4) {
            player.getAbilities().mayfly = true;
        }
    }

    /**
     * 卸下时清理。注意：如果替换的装备依然含有这个 modifier（例如换了另一件也带 Blood_Wing 的护甲），则不关闭飞行。
     * 也保护创意/旁观模式（不影响它们的飞行）。
     */
    @Override
    public void onUnequip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;

        // 如果替换物仍然有该 modifier，则不关闭（例如 swap 同类装备）
        IToolStackView replacement = context.getReplacementTool();
        if (replacement != null && replacement.getModifierLevel(this) > 0) {
            return;
        }

        GameType gm = player.gameMode.getGameModeForPlayer();
        if (gm == GameType.SPECTATOR || gm == GameType.CREATIVE) return;

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
    }

    /**
     * 每 tick 调用（当 item 在正确槽位时会传入 isCorrectSlot = true）。
     * 用来处理玩家等级变化（例如穿着的时候升/降级）——只有吸血鬼等级 >= 3 才保持飞行。
     */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(holder instanceof Player player)) return;
        if (!isCorrectSlot) return; // 不是穿在正确槽位则忽略

        Optional<VampirePlayer> opt = VampirePlayer.getOpt(player).resolve();
        if (opt.isPresent() && opt.get().getLevel() >= 3) {
            // 只在 server 上设置能力（ServerPlayer）以保证同步
            if (player instanceof ServerPlayer sp) {
                sp.getAbilities().mayfly = true;
            } else {
                player.getAbilities().mayfly = true;
            }
        } else {
            // 非满足等级或非吸血鬼：关闭飞行（但不影响创造/旁观）
            if (player instanceof ServerPlayer sp) {
                GameType gm = sp.gameMode.getGameModeForPlayer();
                if (gm == GameType.SPECTATOR || gm == GameType.CREATIVE) return;
                sp.getAbilities().mayfly = false;
                sp.getAbilities().flying = false;
            } else {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}