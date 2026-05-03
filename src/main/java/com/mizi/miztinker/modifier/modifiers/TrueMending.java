package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.modules.capacity.OverslimeModule;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class TrueMending extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && world.getGameTime() % 10 == 0 && holder instanceof Player player && player.totalExperience > 0) {

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack targetStack = player.getInventory().getItem(i);
                if (targetStack.isEmpty() || !targetStack.hasTag()) continue;

                boolean repaired = false;

                if (targetStack.getItem() instanceof IModifiable) {
                    ToolStack tStack = ToolStack.from(targetStack);

                    if (tStack.getDamage() > 0) {
                        tStack.setDamage(Math.max(0, tStack.getDamage() - 5));
                        repaired = true;
                    }

                    if (!repaired) {
                        ModifierId overslimeId = ModifierId.tryParse("tconstruct:overslime");
                        if (overslimeId != null && tStack.getModifiers().getLevel(overslimeId) > 0) {

                            int currentShield = OverslimeModule.INSTANCE.getAmount(tStack);
                            int shieldCap = OverslimeModule.getCapacity(tStack);

                            if (currentShield < shieldCap) {
                                OverslimeModule.INSTANCE.addAmount(tStack, 10);
                                repaired = true;
                            }
                        }
                    }

                    if (repaired) tStack.updateStack(targetStack);
                }

                if (!repaired && processNBTRepair(targetStack.getOrCreateTag(), targetStack.getOrCreateTag())) {
                    repaired = true;
                }

                if (repaired) {
                    player.giveExperiencePoints(-1);
                    break;
                }
            }
        }
    }

    private boolean processNBTRepair(CompoundTag rootTag, CompoundTag currentTag) {
        for (String key : currentTag.getAllKeys()) {
            Tag subTag = currentTag.get(key);
            if (subTag == null) continue;

            if (subTag instanceof CompoundTag compound) {
                if (processNBTRepair(rootTag, compound)) return true;
            } else if (subTag instanceof NumericTag numericTag) {
                if (isBlacklistedKey(key) || key.equals("tconstruct:overslime")) continue;

                double val = numericTag.getAsDouble();

                if (key.equalsIgnoreCase("Damage")) {
                    if (val > 0) {
                        writeNumeric(currentTag, key, Math.max(0, val - 5), numericTag);
                        return true;
                    }
                }

                double max = findMax(rootTag, currentTag, key);
                if (val < max) {
                    writeNumeric(currentTag, key, Math.min(max, val + 10), numericTag);
                    return true;
                }
            }
        }
        return false;
    }

    private double findMax(CompoundTag root, CompoundTag current, String key) {
        String lowKey = key.toLowerCase();
        if (lowKey.contains("max") || lowKey.contains("limit") || lowKey.contains("cap")) return Double.NEGATIVE_INFINITY;

        String[] indicators = {"max", "limit", "cap", "capacity"};
        for (String otherKey : current.getAllKeys()) {
            String otherLow = otherKey.toLowerCase();
            if (otherLow.contains(lowKey)) {
                for (String ind : indicators) {
                    if (otherLow.contains(ind) && current.get(otherKey) instanceof NumericTag n) {
                        return n.getAsDouble();
                    }
                }
            }
        }
        return Double.NEGATIVE_INFINITY;
    }

    private void writeNumeric(CompoundTag tag, String key, double value, NumericTag original) {
        if (original instanceof net.minecraft.nbt.ByteTag) tag.putByte(key, (byte) value);
        else if (original instanceof net.minecraft.nbt.ShortTag) tag.putShort(key, (short) value);
        else if (original instanceof net.minecraft.nbt.IntTag) tag.putInt(key, (int) value);
        else if (original instanceof net.minecraft.nbt.LongTag) tag.putLong(key, (long) value);
        else if (original instanceof net.minecraft.nbt.FloatTag) tag.putFloat(key, (float) value);
        else if (original instanceof net.minecraft.nbt.DoubleTag) tag.putDouble(key, value);
    }

    private boolean isBlacklistedKey(String key) {
        String k = key.toLowerCase();
        return k.equals("id") || k.equals("count") || k.equals("slot") || k.contains("uuid") || k.matches("[xyz]");
    }
}