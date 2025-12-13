package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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


    // 飞行中每 2 秒消耗
    private static final int FLYING_DAMAGE = 1;
    private static final int FLYING_DAMAGE_INTERVAL = 40; // 40 tick = 2 秒

    private static final ResourceLocation FLIGHT_GRANTED =
            new ResourceLocation("miztinker", "wizard_flight_granted");
    private static final ResourceLocation WAS_FLYING =
            new ResourceLocation("miztinker", "wizard_was_flying");

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {

        if (level.isClientSide || !(holder instanceof ServerPlayer player)) return;

        ModDataNBT data = tool.getPersistentData();

        // ✅ 主手或副手持有才生效
        boolean inMainHand = stack == player.getMainHandItem();
        boolean inOffHand  = stack == player.getOffhandItem();

        if (!inMainHand && !inOffHand) {
            revokeIfGranted(player, data);
            return;
        }

        // ✅ 不覆盖创造 / 旁观
        GameType gm = player.gameMode.getGameModeForPlayer();
        if (gm == GameType.CREATIVE || gm == GameType.SPECTATOR) {
            revokeIfGranted(player, data);
            return;
        }

        // ✅ 工具损坏禁止飞行
        if (tool.isBroken()) {
            revokeIfGranted(player, data);
            return;
        }

        // ✅ 授予 mayfly（不强制 flying）
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
            data.putBoolean(FLIGHT_GRANTED, true);
        }

        // -----------------------------
        // ✅ 飞行耐久消耗逻辑
        // -----------------------------
        boolean flying = player.getAbilities().flying;


        // ✨ 飞行中每 2 秒消耗
        if (flying) {
            if (level.getGameTime() % FLYING_DAMAGE_INTERVAL == 0) {
                ToolDamageUtil.damage(tool, FLYING_DAMAGE, player, stack);
            }
        }

        data.putBoolean(WAS_FLYING, flying);
    }

    /**
     * ✅ 只撤销本 modifier 授予的飞行，不影响其他来源
     */
    private void revokeIfGranted(ServerPlayer player, ModDataNBT data) {

        data.remove(WAS_FLYING);

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