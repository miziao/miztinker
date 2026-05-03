package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class Sandwich extends NoLevelsModifier implements SlotStackModifierHook, GeneralInteractionModifierHook, TooltipModifierHook {

    private static final ResourceLocation FOOD_STORAGE = ResourceLocation.fromNamespaceAndPath("mizi", "sandwich_data");
    private static final String TAG_ABSORBED_LIST = "AbsorbedItems";
    private static final String TAG_HUNGER = "TotalHunger";
    private static final String TAG_SATURATION = "TotalSaturation";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK, ModifierHooks.GENERAL_INTERACT, ModifierHooks.TOOLTIP);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry modifier, ItemStack held, Slot slot, Player player, SlotAccess access) {
        if (held.isEdible()) {
            ModDataNBT data = tool.getPersistentData();
            CompoundTag sandwichNBT = data.getCompound(FOOD_STORAGE);
            ListTag absorbedList = sandwichNBT.getList(TAG_ABSORBED_LIST, Tag.TAG_STRING);

            String itemRegistryName = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(held.getItem())).toString();

            for (int i = 0; i < absorbedList.size(); i++) {
                if (absorbedList.getString(i).equals(itemRegistryName)) {
                    player.displayClientMessage(Component.literal("§c[三明治] 已经吸收过这种配料了！"), true);
                    return false;
                }
            }

            FoodProperties food = held.getItem().getFoodProperties(held, player);
            if (food != null) {
                absorbedList.add(StringTag.valueOf(itemRegistryName));
                sandwichNBT.put(TAG_ABSORBED_LIST, absorbedList);

                sandwichNBT.putInt(TAG_HUNGER, sandwichNBT.getInt(TAG_HUNGER) + food.getNutrition());
                sandwichNBT.putFloat(TAG_SATURATION, sandwichNBT.getFloat(TAG_SATURATION) + food.getSaturationModifier());

                data.put(FOOD_STORAGE, sandwichNBT);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.displayClientMessage(Component.literal("§6[三明治] §a成功加入了配料: " + held.getHoverName().getString()), true);

                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (source == InteractionSource.RIGHT_CLICK && !tool.isBroken() && player.canEat(false)) {
            GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onFinishUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
        if (entity instanceof Player player && !tool.isBroken()) {
            ModDataNBT data = tool.getPersistentData();
            CompoundTag sandwichNBT = data.getCompound(FOOD_STORAGE);

            int hunger = sandwichNBT.contains(TAG_HUNGER) ? sandwichNBT.getInt(TAG_HUNGER) : 5;
            float saturation = sandwichNBT.contains(TAG_SATURATION) ? sandwichNBT.getFloat(TAG_SATURATION) : 0.6F;

            player.getFoodData().eat(hunger, saturation);

            ListTag absorbedList = sandwichNBT.getList(TAG_ABSORBED_LIST, Tag.TAG_STRING);
            for (int i = 0; i < absorbedList.size(); i++) {
                ResourceLocation id = ResourceLocation.parse(absorbedList.getString(i));
                ItemStack dummyStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(id)));
                dummyStack.getItem().finishUsingItem(dummyStack, player.level(), player);
            }

            Level world = player.level();
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, 1.0F);

            ToolDamageUtil.damageAnimated(tool, 10, player, player.getUsedItemHand());
        }
    }

    @Override
    public UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
        return 32;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltips, TooltipKey tooltipKey, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        if (data.contains(FOOD_STORAGE, Tag.TAG_COMPOUND)) {
            CompoundTag nbt = data.getCompound(FOOD_STORAGE);
            int hunger = nbt.getInt(TAG_HUNGER);
            float sat = nbt.getFloat(TAG_SATURATION);
            ListTag list = nbt.getList(TAG_ABSORBED_LIST, Tag.TAG_STRING);

            tooltips.add(Component.literal("§e已吸收配料: §f" + list.size()));
            if (!list.isEmpty() && tooltipKey == TooltipKey.SHIFT) {
                for (int i = 0; i < list.size(); i++) {
                    ResourceLocation id = ResourceLocation.parse(list.getString(i));
                    tooltips.add(Component.literal(" §7- " + Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(id)).getDescription().getString()));
                }
            }
            tooltips.add(Component.literal("§7总回复: §6" + hunger + " 饥饿值 §e/ " + String.format("%.1f", sat) + " 饱和度"));
        } else {
            tooltips.add(Component.literal("§8空空如也的三明治面胚"));
        }
    }
}