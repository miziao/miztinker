package com.mizi.miztinker.modifier.modifiers.base;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber(modid = "miztinker")
public class MercyEventHandler {

    private static final ResourceLocation MERCY_ID =
            ResourceLocation.fromNamespaceAndPath("miztinker", "mercy");

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target == null || target.level().isClientSide) {
            return;
        }

        ItemStack heldItem = attacker.getMainHandItem();
        if (heldItem.isEmpty()) {
            return;
        }

        ToolStack tool;

        try {
            tool = ToolStack.copyFrom(heldItem);
        } catch (Exception ignored) {
            return;
        }

        if (tool.isBroken() || !hasMercy(tool)) {
            return;
        }

        float currentHealth = target.getHealth();
        float incomingDamage = event.getAmount();

        if (currentHealth <= 1.0f) {
            event.setCanceled(true);
            event.setAmount(0.0f);
            return;
        }

        if (incomingDamage >= currentHealth) {
            event.setAmount(currentHealth - 1.0f);
        }
    }

    private static boolean hasMercy(ToolStack tool) {
        return tool.getModifierList().stream()
                .anyMatch(entry -> MERCY_ID.equals(entry.getModifier().getId()));
    }
}