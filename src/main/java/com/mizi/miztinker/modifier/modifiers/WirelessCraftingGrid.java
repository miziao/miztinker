package com.mizi.miztinker.modifier.modifiers;

import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Objects;

import static com.mizi.miztinker.miztinker.getResource;

public class WirelessCraftingGrid extends NoLevelsModifier implements
        BlockInteractionModifierHook,
        GeneralInteractionModifierHook {

    private static final String RS_BOUND_POS = "rs_bound_pos";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        Player player = context.getPlayer();
        if (player == null || !player.isCrouching()) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock());

        if (blockId != null && blockId.getNamespace().equals("refinedstorage")) {
            if (!blockId.getPath().contains("controller")) return InteractionResult.SUCCESS;

            if (!level.isClientSide) {
                ModDataNBT data = tool.getPersistentData();
                CompoundTag posTag = new CompoundTag();
                posTag.putLong("pos", pos.asLong());
                posTag.putString("dim", level.dimension().location().toString());
                data.put(getResource(RS_BOUND_POS), posTag);

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.2f);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide) return InteractionResult.PASS;

        if (source == InteractionSource.RIGHT_CLICK && !player.isCrouching()) {
            ModDataNBT data = tool.getPersistentData();
            if (!data.contains(getResource(RS_BOUND_POS))) return InteractionResult.FAIL;

            int slotIndex = (hand == InteractionHand.MAIN_HAND) ? player.getInventory().selected : 40;
            openWirelessGrid((ServerPlayer) player, player.getItemInHand(hand), slotIndex);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static void openWirelessGrid(ServerPlayer player, ItemStack tinkerTool, int slotIndex) {
        var creativeItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("refinedstorageaddons", "creative_wireless_crafting_grid"));
        if (creativeItem == null) return;

        ModDataNBT tinkerData = ToolStack.from(tinkerTool).getPersistentData();
        CompoundTag posData = tinkerData.getCompound(getResource(RS_BOUND_POS));

        BlockPos pos = BlockPos.of(posData.getLong("pos"));
        String dimStr = posData.getString("dim");

        ResourceLocation dimId = ResourceLocation.parse(dimStr);
        var targetLevel = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimId));

        if (targetLevel != null) {
            targetLevel.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        }

        var node = API.instance().getNetworkNodeManager(targetLevel != null ? targetLevel : player.serverLevel()).getNode(pos);
        var network = (node != null) ? node.getNetwork() : API.instance().getNetworkManager(targetLevel != null ? targetLevel : player.serverLevel()).getNetwork(pos);

        if (network != null) {
            ItemStack proxyStack = new ItemStack(creativeItem);
            CompoundTag proxyTag = proxyStack.getOrCreateTag();

            if (tinkerTool.hasTag() && tinkerTool.getTag() != null) {
                proxyTag.merge(tinkerTool.getTag());
            }

            proxyTag.putString("Dimension", dimStr);
            proxyTag.putInt("NodeX", pos.getX());
            proxyTag.putInt("NodeY", pos.getY());
            proxyTag.putInt("NodeZ", pos.getZ());

            proxyTag.putInt("Type", 1);
            proxyTag.putBoolean("Creative", true);
            proxyTag.putInt("Energy", 32000);
            proxyTag.putBoolean("RangeFull", true);

            // 3. 匠魂代理元数据
            proxyTag.putBoolean("is_tinker_proxy", true);
            proxyTag.putString("original_tinker_id", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(tinkerTool.getItem())).toString());

            player.getInventory().setItem(slotIndex, proxyStack);
            player.containerMenu.broadcastChanges();

            network.getItemStorageCache().flush();
            network.getNetworkItemManager().open(player, proxyStack, new PlayerSlot(slotIndex));

            API.instance().getGridManager().openGrid(ResourceLocation.parse("refinedstorageaddons:wireless_crafting_grid"), player, proxyStack, new PlayerSlot(slotIndex));
        }
    }
}