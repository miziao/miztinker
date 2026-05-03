package com.mizi.miztinker.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mixin(value = ToolLevellingUtil.class, remap = false)
public class MiztinkerLearningDeviceMixin {

    @Unique
    private static final ModifierId mizi$LEARNING_DEVICE_ID = new ModifierId("miztinker", "learningdevice");
    @Unique
    private static final ModifierId mizi$IMPROVABLE_ID = new ModifierId("tinkerslevellingaddon", "improvable");

    @Unique
    private static final ThreadLocal<Boolean> mizi$isDistributing = ThreadLocal.withInitial(() -> false);

    @Inject(method = "addExperience", at = @At("HEAD"), cancellable = true)
    private static void mizi$onAddExperience(ToolStack tool, int amount, ServerPlayer player, CallbackInfo ci) {
        if (player == null || amount <= 0 || tool == null) return;
        if (mizi$isDistributing.get()) return;

        if (mizi$shouldDistribute(player, tool)) {
            mizi$isDistributing.set(true);
            try {
                mizi$distributeToInventory(player, amount);
                ci.cancel();
            } finally {
                mizi$isDistributing.set(false);
            }
        }
    }

    @Unique
    private static boolean mizi$shouldDistribute(ServerPlayer player, ToolStack sourceTool) {
        if (sourceTool.getModifierLevel(mizi$LEARNING_DEVICE_ID) > 0) return true;

        for (ItemStack armor : player.getInventory().armor) {
            if (!armor.isEmpty() && armor.getItem() instanceof IModifiable) {
                if (ToolStack.from(armor).getModifierLevel(mizi$LEARNING_DEVICE_ID) > 0) return true;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof IModifiable) {
            return ToolStack.from(offhand).getModifierLevel(mizi$LEARNING_DEVICE_ID) > 0;
        }
        return false;
    }

    @Unique
    private static void mizi$distributeToInventory(ServerPlayer player, int amount) {
        for (ItemStack stack : player.getInventory().items) {
            mizi$tryAddExperience(stack, player, amount);
        }

        for (ItemStack stack : player.getInventory().armor) {
            mizi$tryAddExperience(stack, player, amount);
        }

        mizi$tryAddExperience(player.getOffhandItem(), player, amount);

        player.containerMenu.broadcastChanges();
    }

    @Unique
    private static void mizi$tryAddExperience(ItemStack stack, ServerPlayer player, int amount) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IModifiable)) return;

        try {
            ToolStack target = ToolStack.from(stack);
            if (target.getModifierLevel(mizi$IMPROVABLE_ID) > 0) {
                ToolLevellingUtil.addExperience(target, amount, player);
                stack.setTag(target.createStack().getTag());
            }
        } catch (Exception ignored) {
        }
    }
}