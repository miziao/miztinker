package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
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
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class Mending extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    private static final ResourceLocation OVERSLIME_RL = ResourceLocation.fromNamespaceAndPath("tconstruct", "overslime");
    private static final ResourceLocation OVERSLIME_CAP_RL = ResourceLocation.fromNamespaceAndPath("tconstruct", "overslime_cap");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && world.getGameTime() % 5 == 0 && holder instanceof Player player && player.isCrouching()) {

            if (player.totalExperience <= 0 && player.experienceLevel <= 0) return;

            for (EquipmentSlot slot : SLOTS) {
                ItemStack slotStack = player.getItemBySlot(slot);

                if (slotStack.isEmpty() || !(slotStack.getItem() instanceof IModifiable)) continue;

                if (ModifierUtil.getModifierLevel(slotStack, this.getId()) > 0) {
                    ToolStack tStack = ToolStack.from(slotStack);
                    boolean repaired = false;

                    int damage = tStack.getDamage();
                    if (damage > 0) {
                        tStack.setDamage(Math.max(0, damage - 10));
                        repaired = true;
                    }
                    else if (tryRepairOverslime(tStack)) {
                        repaired = true;
                    }

                    if (repaired) {
                        tStack.updateStack(slotStack);
                        player.giveExperiencePoints(-1);
                        return;
                    }
                }
            }
        }
    }

    private boolean tryRepairOverslime(ToolStack tStack) {
        ModDataNBT persistentData = tStack.getPersistentData();
        ModDataNBT volatileData = (ModDataNBT) tStack.getVolatileData();

        int current = persistentData.getInt(OVERSLIME_RL);

        int cap = volatileData.getInt(OVERSLIME_CAP_RL);
        if (cap <= 0) {
            cap = persistentData.getInt(OVERSLIME_CAP_RL);
        }

        if (cap > 0 && current < cap) {
            persistentData.putInt(OVERSLIME_RL, Math.min(cap, current + 10));
            return true;
        }
        return false;
    }
}