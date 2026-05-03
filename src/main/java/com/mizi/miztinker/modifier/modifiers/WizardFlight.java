package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

public class WizardFlight extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final int FLYING_DAMAGE = 1;
    private static final int FLYING_DAMAGE_INTERVAL = 40;

    private static final ResourceLocation FLIGHT_GRANTED =
            ResourceLocation.fromNamespaceAndPath("miztinker", "wizard_flight_granted");

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (level.isClientSide || !(holder instanceof ServerPlayer player)) return;

        ModDataNBT data = tool.getPersistentData();

        boolean isHolding = (stack == player.getMainHandItem()) || (stack == player.getOffhandItem());

        if (!isHolding) {
            revokeIfGranted(player, data);
            return;
        }

        GameType gm = player.gameMode.getGameModeForPlayer();
        if (gm == GameType.CREATIVE || gm == GameType.SPECTATOR || tool.isBroken()) {
            revokeIfGranted(player, data);
            return;
        }

        if (!player.getAbilities().mayfly) {
            enableFlight(player, data);
        }

        if (player.getAbilities().flying) {
            if (level.getGameTime() % FLYING_DAMAGE_INTERVAL == 0) {
                ToolDamageUtil.damage(tool, FLYING_DAMAGE, player, stack);

                if (tool.isBroken()) {
                    revokeIfGranted(player, data);
                }
            }
        }
    }

    private void enableFlight(ServerPlayer player, ModDataNBT data) {
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
        data.putBoolean(FLIGHT_GRANTED, true);
    }

    private void revokeIfGranted(ServerPlayer player, ModDataNBT data) {
        if (!data.getBoolean(FLIGHT_GRANTED)) return;

        GameType gm = player.gameMode.getGameModeForPlayer();
        if (gm == GameType.CREATIVE || gm == GameType.SPECTATOR) return;

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();

        data.remove(FLIGHT_GRANTED);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}