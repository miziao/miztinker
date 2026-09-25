package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.RawDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;

import javax.annotation.Nullable;
import java.util.List;

public class Scrap extends Modifier implements ValidateModifierHook, RawDataModifierHook, InventoryTickModifierHook {

    private static final ResourceLocation ORIGINAL_MATERIALS_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "scrap.original_materials");
    private static final ResourceLocation SHOULD_EXPLODE = ResourceLocation.fromNamespaceAndPath("miztinker", "scrap.should_explode");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.VALIDATE, ModifierHooks.RAW_DATA, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    @Nullable
    public net.minecraft.network.chat.Component validate(IToolStackView tool, ModifierEntry modifier) {
        ModDataNBT data = tool.getPersistentData();
        List<MaterialVariant> currentMaterials = tool.getMaterials().getList();

        if (data.contains(ORIGINAL_MATERIALS_KEY)) {
            CompoundTag root = data.getCopy();
            ListTag originalList = root.getList(ORIGINAL_MATERIALS_KEY.toString(), 8);

            boolean isChanged = originalList.size() != currentMaterials.size();
            if (!isChanged) {
                for (int i = 0; i < currentMaterials.size(); i++) {
                    String currentId = currentMaterials.get(i).getVariant().toString();
                    String originalId = originalList.getString(i);
                    if (!currentId.equals(originalId)) {
                        isChanged = true;
                        break;
                    }
                }
            }

            if (isChanged) {
                data.putBoolean(SHOULD_EXPLODE, true);
            }
        }
        return null;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int slotIndex, boolean isSelected, boolean isCorrectSlot, ItemStack itemStack) {
        if (!level.isClientSide && tool.getPersistentData().getBoolean(SHOULD_EXPLODE)) {
            level.explode(null, holder.getX(), holder.getY(), holder.getZ(), 6.0F, true, Level.ExplosionInteraction.BLOCK);
            itemStack.setCount(0);
        }
    }

    @Override
    public void addRawData(IToolStackView tool, ModifierEntry modifier, RestrictedCompoundTag restrictedData) {
        ModDataNBT data = tool.getPersistentData();
        if (!data.contains(ORIGINAL_MATERIALS_KEY)) {
            ListTag list = new ListTag();
            for (MaterialVariant variant : tool.getMaterials().getList()) {
                list.add(net.minecraft.nbt.StringTag.valueOf(variant.getVariant().toString()));
            }
            data.put(ORIGINAL_MATERIALS_KEY, list);
        }
    }

    @Override
    public void removeRawData(IToolStackView tool, Modifier modifier, RestrictedCompoundTag restrictedData) {
    }
}