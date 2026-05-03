package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.ArrayList;
import java.util.List;

public class Metalfoes_Fusion extends NoLevelsModifier implements InventoryTickModifierHook, SlotStackModifierHook {

    private static final ResourceLocation METALFOES_STORAGE = ResourceLocation.fromNamespaceAndPath("miztinker", "metalfoes_fusion_data");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView slotTool, ModifierEntry modifier, ItemStack held, Slot slot, Player player, SlotAccess access) {
        if (held.is(Items.POTION) || held.is(Items.SPLASH_POTION) || held.is(Items.LINGERING_POTION)) {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(held);
            if (effects.isEmpty()) return false;

            ModDataNBT data = slotTool.getPersistentData();
            CompoundTag potionMap;

            if (data.contains(METALFOES_STORAGE, Tag.TAG_COMPOUND)) {
                potionMap = data.get(METALFOES_STORAGE, CompoundTag::getCompound);
            } else {
                potionMap = new CompoundTag();
            }

            boolean success = false;
            for (MobEffectInstance inst : effects) {
                MobEffect effect = inst.getEffect();
                ResourceLocation effectKey = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                if (effectKey == null) continue;

                String effectId = effectKey.toString();
                int currentAmplifier = potionMap.contains(effectId) ? potionMap.getInt(effectId) : -1;

                int newAmplifier = Math.min(currentAmplifier + inst.getAmplifier() + 1, 254);
                potionMap.putInt(effectId, newAmplifier);

                player.displayClientMessage(Component.literal("§6[炼装融合] §b炼金注入：§f" +
                        effect.getDisplayName().getString() + " §e-> 魔药等级: " + (newAmplifier + 1)), true);
                success = true;
            }

            if (success) {
                data.put(METALFOES_STORAGE, potionMap);
                if (!player.getAbilities().instabuild) {
                    access.set(new ItemStack(Items.GLASS_BOTTLE));
                }
                return true;
            }
        }
        else if (held.is(Items.GLASS_BOTTLE)) {
            ModDataNBT data = slotTool.getPersistentData();

            if (data.contains(METALFOES_STORAGE, Tag.TAG_COMPOUND)) {
                CompoundTag potionMap = data.get(METALFOES_STORAGE, CompoundTag::getCompound);

                ItemStack customPotion = new ItemStack(Items.POTION);
                customPotion.setHoverName(Component.literal("§d重炼装融合液"));

                List<MobEffectInstance> effectsToExport = new ArrayList<>();

                for (String key : potionMap.getAllKeys()) {
                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.parse(key));
                    if (effect != null) {
                        int amp = potionMap.getInt(key);
                        effectsToExport.add(new MobEffectInstance(effect, 1200, amp));
                    }
                }

                if (!effectsToExport.isEmpty()) {
                    PotionUtils.setCustomEffects(customPotion, effectsToExport);
                    PotionUtils.setPotion(customPotion, Potions.THICK);

                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                        if (held.isEmpty()) {
                            access.set(customPotion);
                        } else {
                            if (!player.getInventory().add(customPotion)) {
                                player.drop(customPotion, false);
                            }
                        }
                    } else {
                        player.getInventory().add(customPotion);
                    }

                    data.remove(METALFOES_STORAGE);
                    player.displayClientMessage(Component.literal("§a[炼装融合] 效果已重洗并提取！"), true);
                    return true;
                }
            } else {
                player.displayClientMessage(Component.literal("§8[炼装融合] 物质不足，无法进行重联。"), true);
                return false;
            }
        }
        return false;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && isCorrectSlot && holder.tickCount % 20 == 0) {
            ModDataNBT data = tool.getPersistentData();

            if (data.contains(METALFOES_STORAGE, Tag.TAG_COMPOUND)) {
                CompoundTag potionMap = data.get(METALFOES_STORAGE, CompoundTag::getCompound);

                for (String key : potionMap.getAllKeys()) {
                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.parse(key));

                    if (effect != null) {
                        int amp = potionMap.getInt(key);
                        holder.addEffect(new MobEffectInstance(effect, 61, amp, false, false, true));
                    }
                }
            }
        }
    }
}