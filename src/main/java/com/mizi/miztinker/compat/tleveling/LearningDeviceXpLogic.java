package com.mizi.miztinker.compat.tleveling;


import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.item.ItemStack;

import slimeknights.tconstruct.library.modifiers.ModifierId;

import slimeknights.tconstruct.library.tools.item.ModifiableItem;


import slimeknights.tconstruct.library.tools.nbt.ToolStack;



public class LearningDeviceXpLogic {
    private static final ThreadLocal<Boolean> GUARD = ThreadLocal.withInitial(() -> false);
    private static final ModifierId LEARNING_DEVICE = new ModifierId("miztinker:learningdevice");
    private static final ModifierId IMPROVABLE = new ModifierId("tinkerslevellingaddon:improvable");

    public static void copyXpIfAllowed(ServerPlayer player, ToolStack source, int amount) {
        if (GUARD.get() || amount <= 0) return;
        if (source.getModifierLevel(LEARNING_DEVICE) <= 0) return;

        GUARD.set(true);
        try {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty() || !(stack.getItem() instanceof ModifiableItem)) continue;
                if (stack == source.createStack()) continue;

                ToolStack targetTool = ToolStack.from(stack);
                if (targetTool.getModifierLevel(IMPROVABLE) > 0) {
                    try {
                        Class<?> utilClass = Class.forName("pyre.tinkerslevellingaddon.util.ToolLevellingUtil");
                        java.lang.reflect.Method addExp = utilClass.getMethod("addExperience", ToolStack.class, int.class, ServerPlayer.class);
                        addExp.invoke(null, targetTool, amount, player);

                        player.getInventory().setItem(i, targetTool.createStack());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            player.containerMenu.broadcastChanges();
        } finally {
            GUARD.set(false);
        }
    }
}
