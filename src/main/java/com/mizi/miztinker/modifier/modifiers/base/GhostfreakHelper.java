package com.mizi.miztinker.modifier.modifiers.base;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class GhostfreakHelper {

    public static final ResourceLocation GHOSTFREAK_ACTIVE =
            new ResourceLocation("miztinker", "ghostfreak_active");

    private static final String GHOSTFREAK_LAST =
            "miztinker_ghostfreak_last";

    /** 是否被流体抑制 */
    public static boolean isDisabledByFluid(Player player) {
        return player.isInFluidType();
    }

    /** 当前 tick 是否真正处于鬼影状态 */
    public static boolean isGhostActive(Player player) {
        return hasGhostTag(player) && !isDisabledByFluid(player);
    }

    /** 是否拥有 Ghostfreak 状态 Tag（不考虑流体） */
    public static boolean hasGhostTag(Player player) {
        return player.getPersistentData()
                .getBoolean(GHOSTFREAK_ACTIVE.toString());
    }

    /** 写入 / 移除 Ghostfreak 状态 Tag */
    public static void setGhostActive(Player player, boolean active) {
        if (active) {
            player.getPersistentData()
                    .putBoolean(GHOSTFREAK_ACTIVE.toString(), true);
        } else {
            player.getPersistentData()
                    .remove(GHOSTFREAK_ACTIVE.toString());
        }
    }
}