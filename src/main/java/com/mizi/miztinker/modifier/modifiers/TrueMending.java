package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
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
        if (!world.isClientSide && world.getGameTime() % 10 == 0 && holder instanceof Player player) {
            if (player.totalExperience > 0) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack targetStack = player.getInventory().getItem(i);
                    if (targetStack.isEmpty() || !targetStack.hasTag()) continue;

                    boolean repaired = false;

                    if (targetStack.getItem() instanceof slimeknights.tconstruct.library.tools.item.IModifiable) {
                        ToolStack tStack = ToolStack.from(targetStack);
                        if (tStack.getDamage() > 0) {
                            int repairAmount = entry.getLevel() * 5;
                            tStack.setDamage(Math.max(0, tStack.getDamage() - repairAmount));
                            tStack.updateStack(targetStack);
                            repaired = true;
                        }
                    }

                    if (recursiveRepair(targetStack.getOrCreateTag(), targetStack.getOrCreateTag(), entry.getLevel())) {
                        repaired = true;
                    }

                    if (repaired) {
                        player.giveExperiencePoints(-1);
                        break;
                    }
                }
            }
        }
    }

    private boolean recursiveRepair(CompoundTag rootTag, CompoundTag currentTag, int level) {
        boolean anyRepaired = false;

        for (String key : currentTag.getAllKeys()) {
            Tag subTag = currentTag.get(key);
            if (subTag == null) continue;

            if (subTag.getId() == Tag.TAG_COMPOUND) {
                if (recursiveRepair(rootTag, (CompoundTag) subTag, level)) {
                    anyRepaired = true;
                }
            }
            else if (isNumeric(subTag.getId())) {
                if (isBlacklistedKey(key)) continue;

                double currentValue = getNumericValue(currentTag, key);

                if (key.equalsIgnoreCase("Damage")) {
                    if (currentValue > 0) {
                        double repairAmount = 5.0 * level;
                        setNumericValue(currentTag, key, Math.max(0, currentValue - repairAmount), subTag.getId());
                        anyRepaired = true;
                    }
                } else {
                    double maxValue = findAssociatedMax(rootTag, currentTag, key);
                    if (currentValue < maxValue) {
                        double repairAmount = 10.0 * level;
                        double newValue = Math.min(maxValue, currentValue + repairAmount);
                        setNumericValue(currentTag, key, newValue, subTag.getId());

                        if (key.equals("tconstruct:overslime") && rootTag.contains("tic_stats")) {
                            CompoundTag persistent = rootTag.getCompound("tic_persistent");
                            setNumericValue(persistent, "tconstruct:overslime", newValue, subTag.getId());
                        }
                        anyRepaired = true;
                    }
                }
            }
        }
        return anyRepaired;
    }

    private boolean isNumeric(byte id) {
        return id >= 1 && id <= 6;
    }

    private boolean isBlacklistedKey(String key) {
        String k = key.toLowerCase();
        return k.equals("id") || k.equals("count") || k.equals("slot") || k.equals("x") || k.equals("y") || k.equals("z") || k.contains("uuid");
    }

    private double findAssociatedMax(CompoundTag root, CompoundTag current, String key) {
        if (key.equals("tconstruct:overslime")) {
            if (current.contains("tconstruct:overslime_cap")) return getNumericValue(current, "tconstruct:overslime_cap");
            if (root.contains("tic_volatile_data")) {
                CompoundTag volatileData = root.getCompound("tic_volatile_data");
                if (volatileData.contains("tconstruct:overslime_cap")) return getNumericValue(volatileData, "tconstruct:overslime_cap");
            }
            return 10000.0;
        }

        String lowKey = key.toLowerCase();
        for (String targetKey : current.getAllKeys()) {
            String lowTarget = targetKey.toLowerCase();

            if (lowTarget.contains(lowKey) &&
                    (lowTarget.contains("max") || lowTarget.contains("limit") || lowTarget.contains("cap") || lowTarget.contains("capacity"))) {
                if (!lowTarget.equals(lowKey)) {
                    return getNumericValue(current, targetKey);
                }
            }
        }

        return Double.NEGATIVE_INFINITY;
    }

    private double getNumericValue(CompoundTag tag, String key) {
        Tag t = tag.get(key);
        if (t instanceof net.minecraft.nbt.NumericTag n) return n.getAsDouble();
        return 0;
    }

    private void setNumericValue(CompoundTag tag, String key, double value, byte type) {
        switch (type) {
            case 1 -> tag.putByte(key, (byte) value);
            case 2 -> tag.putShort(key, (short) value);
            case 3 -> tag.putInt(key, (int) value);
            case 4 -> tag.putLong(key, (long) value);
            case 5 -> tag.putFloat(key, (float) value);
            case 6 -> tag.putDouble(key, value);
        }
    }
}