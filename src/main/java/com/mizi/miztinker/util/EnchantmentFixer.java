package com.mizi.miztinker.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "miztinker")
public class EnchantmentFixer {

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.getItem() instanceof ModifiableItem) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(weapon);
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    entry.getKey().doPostAttack(attacker, event.getEntity(), entry.getValue());
                    float bonus = entry.getKey().getDamageBonus(entry.getValue(), event.getEntity().getMobType());
                    event.setAmount(event.getAmount() + bonus);
                }
            }
        }
    }


}