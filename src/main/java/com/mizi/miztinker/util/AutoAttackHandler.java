package com.mizi.miztinker.util;

import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AutoAttackHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.screen != null || !mc.options.keyAttack.isDown()) return;

        if (mc.missTime > 0) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof IModifiable)) return;

        ToolStack tool = ToolStack.from(stack);

        if (!tool.isBroken() && tool.getModifierLevel(MiztinkerModifiers.feralClaws.getId()) > 0) {

            if (player.getAttackStrengthScale(0) >= 0.9f) {
                performAutoAttack(mc, player);

                player.resetAttackStrengthTicker();

                double attackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
                mc.missTime = (int) Math.max(1, (20.0 / attackSpeed) - 1);
            }
        }
    }

    private static void performAutoAttack(Minecraft mc, Player player) {
        if (mc.gameMode == null) return;

        HitResult hitResult = mc.hitResult;
        ItemStack stack = player.getMainHandItem();

        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            mc.gameMode.attack(player, ((EntityHitResult) hitResult).getEntity());
        }

        LeftClickModifierHook.handleLeftClick(stack, player, EquipmentSlot.MAINHAND);

        player.swing(InteractionHand.MAIN_HAND);
    }
}