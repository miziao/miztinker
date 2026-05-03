package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.util.SmeltMapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.HashSet;
import java.util.Set;

public class AutosmeltIGuess extends NoLevelsModifier implements BlockInteractionModifierHook {

    private static final ResourceLocation WHITELIST_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "smelt_whitelist");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT);
    }

    @Override
    public int getPriority() {
        return 150;
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        Player player = context.getPlayer();
        if (player == null || source != InteractionSource.RIGHT_CLICK || context.getLevel().isClientSide) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (be == null) return InteractionResult.PASS;

        return be.getCapability(ForgeCapabilities.ITEM_HANDLER).map(handler -> {
            SmeltMapManager.bakeRecipes(level);
            ModDataNBT persistentData = tool.getPersistentData();

            if (player.isShiftKeyDown()) {
                Set<String> foundIds = new HashSet<>();
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                        if (key != null) foundIds.add(key.toString());
                    }
                }

                if (foundIds.isEmpty()) {
                    persistentData.remove(WHITELIST_KEY);
                    player.displayClientMessage(Component.translatable("message.miztinker.autosmelt.cleared"), true);
                } else {
                    ListTag list = new ListTag();
                    foundIds.forEach(id -> list.add(StringTag.valueOf(id)));
                    persistentData.put(WHITELIST_KEY, list);
                    player.displayClientMessage(Component.translatable("message.miztinker.autosmelt.recorded", foundIds.size()), true);
                }
                return InteractionResult.SUCCESS;
            }

            else {
                Tag tag = persistentData.get(WHITELIST_KEY);
                boolean isWhitelistMode = (tag instanceof ListTag);
                Set<Item> whitelist = new HashSet<>();

                if (isWhitelistMode) {
                    ListTag list = (ListTag) tag;
                    for (int i = 0; i < list.size(); i++) {
                        ResourceLocation itemId = ResourceLocation.tryParse(list.getString(i));
                        if (itemId != null) {
                            Item item = ForgeRegistries.ITEMS.getValue(itemId);
                            if (item != null) whitelist.add(item);
                        }
                    }
                }

                boolean changed = false;
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stackInSlot = handler.getStackInSlot(i);
                    if (stackInSlot.isEmpty()) continue;

                    if (isWhitelistMode && !whitelist.contains(stackInSlot.getItem())) continue;

                    ItemStack result = SmeltMapManager.getResult(stackInSlot.getItem());
                    if (!result.isEmpty()) {
                        int count = stackInSlot.getCount();
                        ItemStack newStack = result.copy();
                        newStack.setCount(count);

                        ItemStack extracted = handler.extractItem(i, count, false);
                        if (!extracted.isEmpty()) {
                            ItemStack remaining = handler.insertItem(i, newStack, false);
                            if (!remaining.isEmpty()) {
                                handler.insertItem(i, extracted, false);
                            } else {
                                changed = true;
                            }
                        }
                    }
                }

                if (changed) {
                    be.setChanged();
                    level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.displayClientMessage(Component.translatable("message.miztinker.autosmelt.success"), true);
                    return InteractionResult.SUCCESS;
                }

                return InteractionResult.CONSUME;
            }
        }).orElse(InteractionResult.PASS);
    }
}