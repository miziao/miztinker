package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.isFromDummmmmmyMod;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.modifierCutting;

public class ExtraterrestrialForcesTetra extends NoLevelsModifier {

    private static final float CUTTING_VALUE = 0.10f;

    public ExtraterrestrialForcesTetra() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;

        if (!target.isAlive() || isFromDummmmmmyMod(target)) return;

        ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhandStack.isEmpty()) return;

        try {
            IToolStackView offhandTool = ToolStack.from(offhandStack);
            if (offhandTool.isBroken() || offhandTool.getModifierLevel(this.getId()) <= 0) return;

            float mainHandDamage = 1.0f;
            ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (!mainHandStack.isEmpty() && mainHandStack.getTag() != null && mainHandStack.getTag().contains("tic_modifiers")) {
                IToolStackView mainHandTool = ToolStack.from(mainHandStack);
                mainHandDamage = mainHandTool.getStats().get(ToolStats.ATTACK_DAMAGE);
            } else if (!mainHandStack.isEmpty()) {
                mainHandDamage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            }

            if (mainHandDamage > 0) {
                modifierCutting(target, player, mainHandDamage, CUTTING_VALUE);
            }

        } catch (Exception ignored) {
        }
    }
}