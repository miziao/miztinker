package com.mizi.miztinker.compat.tleveling;

import net.minecraft.server.level.ServerPlayer;

import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import slimeknights.tconstruct.library.tools.nbt.ToolStack;



public class TLevelingCompat {



    private static final boolean LOADED = isClassPresent("pyre.tinkerslevellingaddon.util.ToolLevellingUtil");



    /** 检测 Tinkers Levelling 是否存在 */

    public static boolean isLoaded() {

        return LOADED;

    }



    public static void tryCopyXp(ServerPlayer player, IToolStackView tool, int amount) {

        if (!LOADED) return;

        if (!(tool instanceof ToolStack ts)) return;



        LearningDeviceXpLogic.copyXpIfAllowed(player, ts, amount);

    }



    /** 软依赖检测方法 */

    private static boolean isClassPresent(String name) {

        try {

            Class.forName(name, false, TLevelingCompat.class.getClassLoader());

            return true;

        } catch (Throwable t) {

            return false;

        }

    }

}