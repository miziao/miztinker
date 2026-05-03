package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.packets.ShieldSyncPacket;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.math.BigInteger;

public class EquivalentArmor extends Modifier implements DamageBlockModifierHook, InventoryTickModifierHook {

    public static final String SHIELD_NBT = "miztinker_emc_shield";
    public static final String SHIELD_VAL = "current_shield";
    public static final String SHIELD_COOLDOWN = "recharge_tick";
    public static final String LAST_TICK = "last_update_tick";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry, EquipmentContext context, EquipmentSlot slot, DamageSource source, float damage) {
        if (!(context.getEntity() instanceof ServerPlayer player) || damage <= 0) return false;

        CompoundTag persistentData = player.getPersistentData();
        CompoundTag shieldData = persistentData.getCompound(SHIELD_NBT);

        if (shieldData.getInt(SHIELD_COOLDOWN) > 0) return false;

        int totalLevel = getTotalLevel(player);
        float max = totalLevel * 2000f;
        float currentShield = shieldData.contains(SHIELD_VAL) ? shieldData.getFloat(SHIELD_VAL) : max;

        return player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).map(knowledge -> {
            BigInteger emcPool = knowledge.getEmc();
            BigInteger emcCost = BigInteger.valueOf((long) (damage * 2000));

            if (emcPool.compareTo(emcCost) >= 0) {
                knowledge.setEmc(emcPool.subtract(emcCost));

                float remainingShield = currentShield - damage;
                if (remainingShield <= 0) {
                    shieldData.putFloat(SHIELD_VAL, 0);
                    int rechargeDelay = Math.max(10, 60 - (totalLevel - 1) * 20);
                    shieldData.putInt(SHIELD_COOLDOWN, rechargeDelay);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sounds.SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.PLAYERS, 1.5f, 0.5f);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sounds.SoundEvents.ANVIL_LAND, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 2.0f);
                } else {
                    shieldData.putFloat(SHIELD_VAL, remainingShield);
                }

                persistentData.put(SHIELD_NBT, shieldData);
                MiztinkerNetwork.sendToPlayer(new ShieldSyncPacket(shieldData.getFloat(SHIELD_VAL), shieldData.getInt(SHIELD_COOLDOWN)), player);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level level, LivingEntity entity, int slotIndex, boolean isSelected, boolean isArmor, ItemStack stack) {
        if (level.isClientSide || !isArmor || !(entity instanceof ServerPlayer player)) return;

        long currentTime = level.getGameTime();
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(SHIELD_NBT)) persistentData.put(SHIELD_NBT, new CompoundTag());
        CompoundTag shieldData = persistentData.getCompound(SHIELD_NBT);

        if (shieldData.getLong(LAST_TICK) == currentTime) return;
        shieldData.putLong(LAST_TICK, currentTime);

        int totalLevel = getTotalLevel(player);
        float max = totalLevel * 2000f;

        if (!shieldData.contains(SHIELD_VAL)) {
            shieldData.putFloat(SHIELD_VAL, max);
            MiztinkerNetwork.sendToPlayer(new ShieldSyncPacket(max, 0), player);
        }

        int cooldown = shieldData.getInt(SHIELD_COOLDOWN);
        if (cooldown > 0) {
            int nextCooldown = cooldown - 1;
            shieldData.putInt(SHIELD_COOLDOWN, nextCooldown);

            if (nextCooldown % ((cooldown < 20) ? 10 : 20) == 0) {
                float pitch = 1.0f + (1.0f - (float)nextCooldown / 60.0f);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.WARDEN_HEARTBEAT, net.minecraft.sounds.SoundSource.PLAYERS, 1.2f, pitch);
            }

            if (nextCooldown == 0) {
                shieldData.putFloat(SHIELD_VAL, max);
                persistentData.put(SHIELD_NBT, shieldData);
                MiztinkerNetwork.sendToPlayer(new ShieldSyncPacket(max, 0), player);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 2.0f);
            } else if (currentTime % 5 == 0) {
                MiztinkerNetwork.sendToPlayer(new ShieldSyncPacket(shieldData.getFloat(SHIELD_VAL), nextCooldown), player);
            }
        } else {
            float current = shieldData.getFloat(SHIELD_VAL);
            if (current < max) {
                float nextShield = Math.min(max, current + (totalLevel * 5.0f));
                shieldData.putFloat(SHIELD_VAL, nextShield);
                persistentData.put(SHIELD_NBT, shieldData);

                if (currentTime % 3 == 0 || nextShield >= max) {
                    MiztinkerNetwork.sendToPlayer(new ShieldSyncPacket(nextShield, 0), player);
                }
            }
        }
    }

    public static int getTotalLevel(LivingEntity entity) {
        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    total += ModifierUtil.getModifierLevel(stack, new slimeknights.tconstruct.library.modifiers.ModifierId("miztinker", "equivalent_armor"));
                }
            }
        }
        return total;
    }
}