package com.mizi.miztinker.modifier.modifiers.base;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import net.minecraft.world.item.ItemStack;

@Mod.EventBusSubscriber(modid = "miztinker")
public class MercyEventHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {

            ItemStack heldItem = attacker.getMainHandItem();

            try {
                ToolStack tool = ToolStack.copyFrom(heldItem);

                boolean hasMercy = tool.getModifierList().stream()
                        .anyMatch(entry -> entry.getModifier().getId().getPath().equals("mercy"));

                if (hasMercy && !tool.isBroken()) {
                    LivingEntity target = event.getEntity();
                    float currentHealth = target.getHealth();
                    float incomingDamage = event.getAmount();

                    if (incomingDamage >= currentHealth) {
                        float cappedDamage = currentHealth - 1.0f;

                        if (cappedDamage < 0) {
                            event.setCanceled(true);
                            event.setAmount(0);
                        } else {
                            event.setAmount(cappedDamage);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}
