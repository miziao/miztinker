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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

public class SearchBeacon extends NoLevelsModifier implements SlotStackModifierHook {

    public static final ResourceLocation DATA_KEY =
            ResourceLocation.fromNamespaceAndPath("mizi", "search_beacon");

    public static final String TAG_TARGET_BLOCK = "target_block";
    public static final String TAG_IS_ACTIVE = "is_active";

    private static final String TYPE_BLOCK = "block";
    private static final String TYPE_FLUID = "fluid";

    private static final String LANG_CLEARED =
            "message.miztinker.search_beacon.cleared";

    private static final String LANG_START =
            "message.miztinker.search_beacon.start";

    private static final String LANG_INVALID_NAME =
            "message.miztinker.search_beacon.invalid_name";

    private static final String LANG_NOT_FOUND =
            "message.miztinker.search_beacon.not_found";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            IToolStackView tool,
            ModifierEntry modifier,
            ItemStack held,
            Slot slot,
            Player player,
            SlotAccess access
    ) {
        ModDataNBT data = tool.getPersistentData();

        if (held.getItem() instanceof BlockItem blockItem) {
            setSearchTarget(data, blockItem.getBlock().defaultBlockState(), player);
            return true;
        }

        if (held.is(net.minecraft.world.item.Items.PAPER) && held.hasCustomHoverName()) {
            String rawName = held.getHoverName().getString().trim();

            if (rawName.isEmpty()) {
                sendInvalidNameMessage(player);
                return true;
            }

            ParsedTarget parsed = parseTarget(rawName);

            if (parsed == null || parsed.id == null) {
                sendInvalidNameMessage(player);
                return true;
            }

            Block targetBlock;

            if (TYPE_FLUID.equals(parsed.type)) {
                if (!BuiltInRegistries.FLUID.containsKey(parsed.id)) {
                    sendNotFoundMessage(player, parsed.id);
                    return true;
                }

                targetBlock = BuiltInRegistries.FLUID
                        .get(parsed.id)
                        .defaultFluidState()
                        .createLegacyBlock()
                        .getBlock();
            } else {
                if (!BuiltInRegistries.BLOCK.containsKey(parsed.id)) {
                    sendNotFoundMessage(player, parsed.id);
                    return true;
                }

                targetBlock = BuiltInRegistries.BLOCK.get(parsed.id);
            }

            if (targetBlock == Blocks.AIR) {
                sendNotFoundMessage(player, parsed.id);
                return true;
            }

            setSearchTarget(data, targetBlock.defaultBlockState(), player);
            return true;
        }

        if (held.isEmpty() && data.contains(DATA_KEY, 10)) {
            data.remove(DATA_KEY);

            sendMessage(player, Component.translatable(LANG_CLEARED));
            player.playSound(SoundEvents.BEACON_DEACTIVATE, 1.0F, 0.8F);

            return true;
        }

        return false;
    }

    private ParsedTarget parseTarget(String rawName) {
        String input = rawName.trim().toLowerCase();

        String type = TYPE_BLOCK;
        String namespace = "minecraft";
        String path;

        String[] parts = input.split(":");

        if (parts.length == 1) {
            path = parts[0];
        } else if (parts.length == 2) {
            if (TYPE_BLOCK.equals(parts[0]) || TYPE_FLUID.equals(parts[0])) {
                type = parts[0];
                path = parts[1];
            } else {
                namespace = parts[0];
                path = parts[1];
            }
        } else if (parts.length == 3) {
            if (!TYPE_BLOCK.equals(parts[0]) && !TYPE_FLUID.equals(parts[0])) {
                return null;
            }

            type = parts[0];
            namespace = parts[1];
            path = parts[2];
        } else {
            return null;
        }

        if (namespace.isEmpty() || path.isEmpty()) {
            return null;
        }

        ResourceLocation id = safeResourceLocation(namespace, path);

        if (id == null) {
            return null;
        }

        return new ParsedTarget(type, id);
    }

    private ResourceLocation safeResourceLocation(String namespace, String path) {
        try {
            return ResourceLocation.fromNamespaceAndPath(namespace, path);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setSearchTarget(ModDataNBT data, BlockState state, Player player) {
        CompoundTag beaconNBT = new CompoundTag();

        beaconNBT.put(TAG_TARGET_BLOCK, NbtUtils.writeBlockState(state));
        beaconNBT.putBoolean(TAG_IS_ACTIVE, true);

        data.put(DATA_KEY, beaconNBT);

        sendMessage(
                player,
                Component.translatable(
                        LANG_START,
                        state.getBlock().getName().getString()
                )
        );

        player.playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 1.5F);
    }

    private void sendInvalidNameMessage(Player player) {
        sendMessage(
                player,
                Component.translatable(LANG_INVALID_NAME)
        );

        player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.0F, 0.6F);
    }

    private void sendNotFoundMessage(Player player, ResourceLocation id) {
        sendMessage(
                player,
                Component.translatable(LANG_NOT_FOUND, id.toString())
        );

        player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.0F, 0.6F);
    }

    private void sendMessage(Player player, Component message) {
        if (player != null) {
            player.displayClientMessage(message, true);
        }
    }

    private record ParsedTarget(String type, ResourceLocation id) {
    }
}