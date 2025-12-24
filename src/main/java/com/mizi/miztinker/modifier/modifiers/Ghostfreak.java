package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.GhostfreakHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import net.minecraft.resources.ResourceLocation;

public class Ghostfreak extends NoLevelsModifier implements SlotStackModifierHook, DamageBlockModifierHook, InventoryTickModifierHook {

    private static final ResourceLocation GHOSTFLIGHT_GRANTED = new ResourceLocation("miztinker", "ghostflight");
    private static final ResourceLocation GHOST_WAS_FLYING = new ResourceLocation("miztinker", "ghost_was_flying");

    @Override
    public boolean overrideOtherStackedOnMe(
            IToolStackView tool,
            ModifierEntry entry,
            ItemStack held,
            Slot slot,
            Player player,
            SlotAccess access
    ) {
        if (!held.isEmpty()) return false; // 必须空手

        boolean active = GhostfreakHelper.hasGhostTag(player);
        GhostfreakHelper.setGhostActive(player, !active);

        if (!active) {
            player.displayClientMessage(
                    Component.literal("§3英雄变身——鬼影！"),
                    true
            );
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        } else {
            player.displayClientMessage(
                    Component.literal("§7解除变身。"),
                    true
            );
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS,
                    0.8F,
                    0.9F
            );
            // 撤销本 modifier 授予的飞行
            revokeFlight(player, tool);
        }

        // 切换状态后立即更新飞行
        updateFlight(player, tool);

        return true;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level level, LivingEntity entity, int slot, boolean selected, boolean correctSlot, ItemStack stack) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        updateFlight(player, tool);
    }

    /** 飞行逻辑：只授予幽灵状态且未被流体抑制的玩家 */
    private void updateFlight(Player player, IToolStackView tool) {
        if (!(player instanceof ServerPlayer sp)) return;
        ModDataNBT data = tool.getPersistentData();

        boolean allowFly = GhostfreakHelper.isGhostActive(player) && !GhostfreakHelper.isDisabledByFluid(player);

        if (allowFly) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                sp.onUpdateAbilities();
                data.putBoolean(GHOSTFLIGHT_GRANTED, true);
            }
        } else {
            revokeFlight(player, tool);
        }

        data.putBoolean(GHOST_WAS_FLYING, player.getAbilities().flying);
    }

    /** 撤销本 modifier 授予的飞行 */
    private void revokeFlight(Player player, IToolStackView tool) {
        if (!(player instanceof ServerPlayer sp)) return;
        ModDataNBT data = tool.getPersistentData();

        if (!data.getBoolean(GHOSTFLIGHT_GRANTED)) return;

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        sp.onUpdateAbilities();

        data.remove(GHOSTFLIGHT_GRANTED);
        data.remove(GHOST_WAS_FLYING);
    }

    /** 无敌逻辑 */
    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry, EquipmentContext context,
                                   EquipmentSlot slot, DamageSource source, float damage) {
        if (context.getEntity() instanceof Player player) {
            return GhostfreakHelper.isGhostActive(player) && !GhostfreakHelper.isDisabledByFluid(player);
        }
        return false;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}