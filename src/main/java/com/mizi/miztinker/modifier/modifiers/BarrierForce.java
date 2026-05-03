package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.EnumSet;
import java.util.List;

public class BarrierForce extends NoLevelsModifier implements InventoryTickModifierHook, EquipmentChangeModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!isCorrectSlot || world.isClientSide) return;

        if (!holder.isInvisible()) {
            holder.setInvisible(true);
        }
        List<Mob> nearbyMobs = world.getEntitiesOfClass(Mob.class, holder.getBoundingBox().inflate(32.0D));
        for (Mob mob : nearbyMobs) {
            if (mob.getTarget() == holder || mob.getLastHurtByMob() == holder) {
                clearMobTarget(mob);
            }
        }

        if (holder instanceof ServerPlayer player) {
            updateTabVisibility(player, true);
        }
    }

    @Override
    public void onEquip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        if (context.getEntity() instanceof ServerPlayer player) {
            player.setInvisible(true);
            updateTabVisibility(player, true);
        }
    }

    @Override
    public void onUnequip(@NotNull IToolStackView tool, @NotNull ModifierEntry entry, EquipmentChangeContext context) {
        LivingEntity entity = context.getEntity();
        if (entity.level().isClientSide) return;

        if (shouldReveal(entity, context.getChangedSlot())) {
            entity.setInvisible(false);
            if (entity instanceof ServerPlayer player) {
                updateTabVisibility(player, false);
            }
        }
    }

    private void updateTabVisibility(ServerPlayer player, boolean hide) {
        ServerLevel level = player.serverLevel();
        ClientboundPlayerInfoUpdatePacket packet;
        if (hide) {
            packet = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                    List.of(player)
            );
        } else {
            packet = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class),
                    List.of(player)
            );
        }
        level.getServer().getPlayerList().broadcastAll(packet);
    }

    public static void clearMobTarget(Mob mob) {
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
        mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
    }

    public static boolean hasBarrierForce(LivingEntity entity) {
        if (entity == null) return false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    try {
                        IToolStackView tool = ToolStack.from(stack);
                        if (tool.getModifierLevel(MiztinkerModifiers.BARRIER_FORCE_STATIC_MODIFIER.get()) > 0) {
                            return true;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return false;
    }

    private boolean shouldReveal(LivingEntity entity, EquipmentSlot currentSlot) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor() && slot != currentSlot) {
                ItemStack armorStack = entity.getItemBySlot(slot);
                if (!armorStack.isEmpty()) {
                    try {
                        IToolStackView otherTool = ToolStack.from(armorStack);
                        if (otherTool.getModifierLevel(this) > 0) return false;
                    } catch (Exception ignored) {}
                }
            }
        }
        return true;
    }
}