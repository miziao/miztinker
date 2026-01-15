package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;
import java.util.Map;

public class Extraterrestrial_Forces_Botania extends NoLevelsModifier implements InventoryTickModifierHook, SlotStackModifierHook, TooltipModifierHook {

    private static final ResourceLocation ENCHANT_STORAGE = new ResourceLocation("miztinker", "extra_enchant_data");
    private static final String VANILLA_ENCHANTMENTS = "Enchantments";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView slotTool, ModifierEntry modifier, ItemStack held, Slot slot, Player player, SlotAccess access) {
        ItemStack toolStack = slot.getItem();
        if (!(slotTool instanceof ToolStack)) return false;

        if (held.is(Items.ENCHANTED_BOOK)) {
            Map<Enchantment, Integer> heldEnchants = EnchantmentHelper.getEnchantments(held);
            if (heldEnchants.isEmpty()) return false;

            ModDataNBT data = slotTool.getPersistentData();
            CompoundTag enchantMap = data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND) ?
                    data.get(ENCHANT_STORAGE, CompoundTag::getCompound) : new CompoundTag();

            boolean changed = false;
            for (Map.Entry<Enchantment, Integer> entry : heldEnchants.entrySet()) {
                Enchantment enchant = entry.getKey();
                ResourceLocation res = ForgeRegistries.ENCHANTMENTS.getKey(enchant);
                if (res == null) continue;

                String id = res.toString();
                int currentLvl = enchantMap.getInt(id);
                int addLvl = entry.getValue();

                int newLvl = Math.min(currentLvl + addLvl, 255);
                enchantMap.putInt(id, newLvl);

                player.displayClientMessage(Component.literal("§d[界外之力] §b附魔注入：§f" +
                        enchant.getFullname(newLvl).getString() + " (等级: " + newLvl + ")"), true);
                changed = true;
            }

            if (changed) {
                data.put(ENCHANT_STORAGE, enchantMap);
                syncToItemStack(toolStack, enchantMap);
                if (!player.getAbilities().instabuild) {
                    access.set(new ItemStack(Items.BOOK));
                }
                return true;
            }
        }
        else if (held.is(Items.BOOK)) {
            ModDataNBT data = slotTool.getPersistentData();
            if (data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND)) {
                CompoundTag enchantMap = data.get(ENCHANT_STORAGE, CompoundTag::getCompound);

                ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                for (String key : enchantMap.getAllKeys()) {
                    Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(key));
                    if (enchant != null) {
                        EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(enchant, enchantMap.getInt(key)));
                    }
                }

                if (!enchantedBook.getEnchantmentTags().isEmpty()) {
                    data.remove(ENCHANT_STORAGE);
                    toolStack.getOrCreateTag().remove(VANILLA_ENCHANTMENTS);

                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                        if (held.isEmpty()) access.set(enchantedBook);
                        else if (!player.getInventory().add(enchantedBook)) player.drop(enchantedBook, false);
                    } else {
                        player.getInventory().add(enchantedBook);
                    }

                    player.displayClientMessage(Component.literal("§a[界外之力] 所有附魔已提取至书中！"), true);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onInventoryTick(IToolStackView toolView, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && holder.tickCount % 20 == 0) {
            ModDataNBT data = toolView.getPersistentData();
            if (data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND)) {
                syncToItemStack(stack, data.get(ENCHANT_STORAGE, CompoundTag::getCompound));
            }
        }
    }

    private void syncToItemStack(ItemStack stack, CompoundTag enchantMap) {
        if (stack.isEmpty()) return;

        ListTag enchantList = new ListTag();
        for (String key : enchantMap.getAllKeys()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", key);
            tag.putShort("lvl", (short) enchantMap.getInt(key));
            enchantList.add(tag);
        }

        if (enchantList.isEmpty()) {
            stack.getOrCreateTag().remove(VANILLA_ENCHANTMENTS);
        } else {
            stack.getOrCreateTag().put(VANILLA_ENCHANTMENTS, enchantList);
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, Player player, List<Component> tooltip, TooltipKey key, TooltipFlag flag) {
        ModDataNBT data = tool.getPersistentData();
        if (data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND)) {
            CompoundTag enchantMap = data.getCompound(ENCHANT_STORAGE);
            if (!enchantMap.isEmpty()) {
                tooltip.add(Component.literal("§d[界外附魔存储]"));
                for (String enchantKey : enchantMap.getAllKeys()) {
                    Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantKey));
                    if (enchant != null) {
                        int lvl = enchantMap.getInt(enchantKey);
                        tooltip.add(Component.literal("  ")
                                .append(enchant.getFullname(lvl))
                                .append(Component.literal(" (" + lvl + ")").withStyle(ChatFormatting.DARK_GRAY)));
                    }
                }
            }
        }
    }
}