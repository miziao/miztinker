package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Mega_big_Magnetic extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {
        if (world.isClientSide()) return;
        if (!(holder instanceof Player player)) return;
        if (!isCorrectSlot) return;

        if (player.tickCount % 20 == 0) {
            HaoranArua(player, 7.0);
        }
    }

    private static void HaoranArua(Player player, double range) {
        Level level = player.level();
        AABB area = new AABB(
                player.getX() - range, player.getY() - range, player.getZ() - range,
                player.getX() + range, player.getY() + range, player.getZ() + range
        );

        double totalDrained = 0.0;

        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living == player || !living.isAlive()) continue;

            float maxHp = living.getMaxHealth();
            float drainAmount = maxHp * 0.2F;

            float actualDrain = Math.min(drainAmount, living.getHealth());

            living.setHealth(living.getHealth() - actualDrain);

            if (living.getHealth() <= 0.5F) {
                living.die(new DamageSource(
                        living.level().registryAccess()
                                .registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(DamageTypes.PLAYER_ATTACK),
                        player
                ));
                living.setHealth(0);
            }

            totalDrained += actualDrain;
        }

        if (totalDrained > 0) {
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                double originalMaxHealth = maxHealthAttr.getBaseValue();
                double newMaxHealth = originalMaxHealth + totalDrained;
                maxHealthAttr.setBaseValue(newMaxHealth);
            }
        }
    }
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}
