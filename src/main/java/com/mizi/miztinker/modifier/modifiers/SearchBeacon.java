package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

public class SearchBeacon extends NoLevelsModifier implements SlotStackModifierHook {

    public static final ResourceLocation DATA_KEY = ResourceLocation.fromNamespaceAndPath("mizi", "search_beacon");
    public static final String TAG_TARGET_BLOCK = "target_block";
    public static final String TAG_IS_ACTIVE = "is_active";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry modifier, ItemStack held, Slot slot, Player player, SlotAccess access) {
        ModDataNBT data = tool.getPersistentData();

        if (held.getItem() instanceof BlockItem blockItem) {
            setSearchTarget(data, blockItem.getBlock().defaultBlockState(), player);
            return true;
        }

        if (held.is(net.minecraft.world.item.Items.PAPER) && held.hasCustomHoverName()) {
            String rawName = held.getHoverName().getString().trim();

            String type = "block";
            String namespace = "minecraft";
            String path = "";

            String[] parts = rawName.split(":");
            if (parts.length == 3) {
                type = parts[0];
                namespace = parts[1];
                path = parts[2];
            } else if (parts.length == 2) {
                if (parts[0].equalsIgnoreCase("block") || parts[0].equalsIgnoreCase("fluid")) {
                    type = parts[0];
                    path = parts[1];
                } else {
                    namespace = parts[0];
                    path = parts[1];
                }
            } else {
                path = parts[0];
            }

            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(namespace, path);
            Block targetBlock;

            if (type.equalsIgnoreCase("fluid")) {
                targetBlock = BuiltInRegistries.FLUID.get(rl).defaultFluidState().createLegacyBlock().getBlock();
            } else {
                targetBlock = BuiltInRegistries.BLOCK.get(rl);
            }

            if (targetBlock != net.minecraft.world.level.block.Blocks.AIR) {
                setSearchTarget(data, targetBlock.defaultBlockState(), player);
                return true;
            }
        }

        if (held.isEmpty() && data.contains(DATA_KEY, 10)) {
            data.remove(DATA_KEY);
            if (player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("message.miztinker.search_beacon.cleared"), true);
            }
            player.playSound(SoundEvents.BEACON_DEACTIVATE, 1.0F, 0.8F);
            return true;
        }

        return false;
    }

    private void setSearchTarget(ModDataNBT data, BlockState state, Player player) {
        CompoundTag beaconNBT = new CompoundTag();
        beaconNBT.put(TAG_TARGET_BLOCK, NbtUtils.writeBlockState(state));
        beaconNBT.putBoolean(TAG_IS_ACTIVE, true);
        data.put(DATA_KEY, beaconNBT);

        if (player.level().isClientSide) {
            player.displayClientMessage(Component.translatable("message.miztinker.search_beacon.start",
                    state.getBlock().getName().getString()), true);
        }
        player.playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 1.5F);
    }
}