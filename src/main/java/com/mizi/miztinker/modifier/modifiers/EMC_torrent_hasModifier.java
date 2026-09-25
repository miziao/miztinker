package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class EMC_torrent_hasModifier {

    public static boolean hasEmcTorrent(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) {
            return false;
        }

        return hasEmcTorrent(player.getMainHandItem())
                || hasEmcTorrent(player.getOffhandItem());
    }

    public static boolean hasEmcTorrent(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        try {
            ToolStack tool = ToolStack.copyFrom(stack);

            if (tool.isBroken()) {
                return false;
            }

            return tool.getModifierList().stream()
                    .anyMatch(entry -> EMC_torrent.EMC_TORRENT_ID.equals(entry.getModifier().getId()));
        } catch (Exception ignored) {
            return false;
        }
    }
}