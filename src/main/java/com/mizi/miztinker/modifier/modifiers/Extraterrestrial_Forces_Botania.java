package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
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
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ProtectionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.EnchantmentModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class Extraterrestrial_Forces_Botania extends NoLevelsModifier implements
        InventoryTickModifierHook, SlotStackModifierHook, TooltipModifierHook,
        EnchantmentModifierHook, MeleeHitModifierHook, MeleeDamageModifierHook,
        ProtectionModifierHook, ToolDamageModifierHook {

    private static final ResourceLocation ENCHANT_STORAGE = ResourceLocation.fromNamespaceAndPath("miztinker", "extra_enchant_data");
    private static final String VANILLA_ENCHANTMENTS = "Enchantments";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.SLOT_STACK, ModifierHooks.TOOLTIP);
        hookBuilder.addHook(this, ModifierHooks.ENCHANTMENTS);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.PROTECTION);
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
    }


    @Override
    public void updateEnchantments(IToolStackView tool, ModifierEntry modifier, Map<Enchantment, Integer> enchantments) {
        ModDataNBT data = tool.getPersistentData();
        if (data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND)) {
            CompoundTag enchantMap = data.getCompound(ENCHANT_STORAGE);
            for (String key : enchantMap.getAllKeys()) {
                Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.parse(key));
                if (enchant != null) {
                    enchantments.put(enchant, enchantments.getOrDefault(enchant, 0) + enchantMap.getInt(key));
                }
            }
        }
    }

    @Override
    public int updateEnchantmentLevel(IToolStackView tool, ModifierEntry modifier, Enchantment enchantment, int level) {
        ModDataNBT data = tool.getPersistentData();
        if (data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND)) {
            CompoundTag enchantMap = data.getCompound(ENCHANT_STORAGE);
            ResourceLocation res = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            if (res != null && enchantMap.contains(res.toString())) {
                return level + enchantMap.getInt(res.toString());
            }
        }
        return level;
    }


    @Override
    public float beforeMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damage, float baseKnockback, float knockback) {
        return knockback;
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity attacker = context.getAttacker();
        LivingEntity target = context.getLivingTarget();
        if (target == null) return;

        int fireAspect = tool.getModifierLevel(this) > 0 ? EnchantmentHelper.getFireAspect(attacker) : 0;
        if (fireAspect > 0) {
            target.setRemainingFireTicks(80 * fireAspect);
        }

        EnchantmentHelper.doPostDamageEffects(attacker, target);
        EnchantmentHelper.doPostHurtEffects(target, attacker);
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        return damage;
    }

    @Override
    public float getProtectionModifier(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slot, DamageSource source, float v) {
        return v;
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int damage, @Nullable LivingEntity holder) {
        return damage;
    }


    @Override
    public boolean overrideOtherStackedOnMe(@NotNull IToolStackView slotTool, @NotNull ModifierEntry modifier, @NotNull ItemStack held, Slot slot, @NotNull Player player, @NotNull SlotAccess access) {
        ItemStack toolStack = slot.getItem();
        if (!(slotTool instanceof ToolStack)) return false;

        if (held.is(Items.ENCHANTED_BOOK)) {
            Map<Enchantment, Integer> heldEnchants = EnchantmentHelper.getEnchantments(held);
            if (heldEnchants.isEmpty()) return false;

            ModDataNBT data = slotTool.getPersistentData();
            CompoundTag enchantMap = data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND) ?
                    data.getCompound(ENCHANT_STORAGE).copy() : new CompoundTag();

            for (Map.Entry<Enchantment, Integer> entry : heldEnchants.entrySet()) {
                ResourceLocation res = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey());
                if (res != null) {
                    String id = res.toString();
                    enchantMap.putInt(id, Math.min(enchantMap.getInt(id) + entry.getValue(), 255));
                    player.displayClientMessage(Component.translatable("message.miztinker.enchant_injected", entry.getKey().getFullname(enchantMap.getInt(id))), true);
                }
            }
            data.put(ENCHANT_STORAGE, enchantMap);
            syncToItemStack(toolStack, enchantMap);
            if (!player.getAbilities().instabuild) access.set(new ItemStack(Items.BOOK));
            return true;
        } else if (held.is(Items.BOOK)) {
            ModDataNBT data = slotTool.getPersistentData();
            if (data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND)) {
                CompoundTag enchantMap = data.getCompound(ENCHANT_STORAGE);
                if (enchantMap.isEmpty()) return false;

                ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                for (String key : enchantMap.getAllKeys()) {
                    Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.parse(key));
                    if (enchant != null) {
                        EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(enchant, enchantMap.getInt(key)));
                    }
                }

                data.remove(ENCHANT_STORAGE);
                toolStack.getOrCreateTag().remove(VANILLA_ENCHANTMENTS);

                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                    if (held.isEmpty()) access.set(enchantedBook);
                    else if (!player.getInventory().add(enchantedBook)) player.drop(enchantedBook, false);
                } else {
                    player.getInventory().add(enchantedBook);
                }
                player.displayClientMessage(Component.translatable("message.miztinker.enchant_extracted"), true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onInventoryTick(IToolStackView toolView, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && holder.tickCount % 20 == 0) {
            ModDataNBT data = toolView.getPersistentData();
            if (data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND)) {
                syncToItemStack(stack, data.getCompound(ENCHANT_STORAGE));
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
        if (enchantList.isEmpty()) stack.getOrCreateTag().remove(VANILLA_ENCHANTMENTS);
        else stack.getOrCreateTag().put(VANILLA_ENCHANTMENTS, enchantList);
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        if (data.contains(ENCHANT_STORAGE, Tag.TAG_COMPOUND)) {
            CompoundTag enchantMap = data.getCompound(ENCHANT_STORAGE);
            if (!enchantMap.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.miztinker.extra_enchant_storage").withStyle(ChatFormatting.LIGHT_PURPLE));
                for (String enchantKey : enchantMap.getAllKeys()) {
                    Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.parse(enchantKey));
                    if (enchant != null) {
                        tooltip.add(Component.literal("  ").append(enchant.getFullname(enchantMap.getInt(enchantKey))));
                    }
                }
            }
        }
    }
}