package com.mizi.miztinker.modifier.modifiers;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.blockentity.grid.WirelessGrid;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.mizi.miztinker.miztinker.getResource;

public class WirelessCraftingGrid extends NoLevelsModifier implements
        BlockInteractionModifierHook,
        GeneralInteractionModifierHook {

    private static final String MOD_ID = "miztinker";

    private static final String RS_BOUND_POS = "rs_bound_pos";
    private static final String RS_GRID_STATE = "miztinker_rs_grid_state";
    private static final String IS_TINKER_PROXY = "is_tinker_proxy";

    private static final String MSG_BOUND =
            "message.miztinker.wireless_crafting_grid.bound";
    private static final String MSG_NOT_BOUND =
            "message.miztinker.wireless_crafting_grid.not_bound";
    private static final String MSG_BOUND_DATA_BROKEN =
            "message.miztinker.wireless_crafting_grid.bound_data_broken";
    private static final String MSG_DIMENSION_MISSING =
            "message.miztinker.wireless_crafting_grid.dimension_missing";
    private static final String MSG_CHUNK_LOAD_FAILED =
            "message.miztinker.wireless_crafting_grid.chunk_load_failed";
    private static final String MSG_NETWORK_MISSING =
            "message.miztinker.wireless_crafting_grid.network_missing";
    private static final String MSG_NO_PERMISSION =
            "message.miztinker.wireless_crafting_grid.no_permission";
    private static final String MSG_OPEN_FAILED =
            "message.miztinker.wireless_crafting_grid.open_failed";

    private static final ResourceLocation RSA_WIRELESS_CRAFTING_GRID =
            ResourceLocation.parse("refinedstorageaddons:wireless_crafting_grid");

    static {
        MinecraftForge.EVENT_BUS.register(ChunkTicketEvents.class);
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult beforeBlockUse(
            IToolStackView tool,
            ModifierEntry modifier,
            UseOnContext context,
            InteractionSource source
    ) {
        Player player = context.getPlayer();

        if (player == null || !player.isCrouching()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock());

        if (blockId == null) {
            return InteractionResult.PASS;
        }

        if (!blockId.getNamespace().equals("refinedstorage")) {
            return InteractionResult.PASS;
        }

        if (!blockId.getPath().contains("controller")) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            ModDataNBT data = tool.getPersistentData();

            CompoundTag posTag = new CompoundTag();
            posTag.putLong("pos", pos.asLong());
            posTag.putString("dim", level.dimension().location().toString());

            data.put(getResource(RS_BOUND_POS), posTag);

            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    1.0f,
                    1.2f
            );

            player.displayClientMessage(Component.translatable(MSG_BOUND), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onToolUse(
            IToolStackView tool,
            ModifierEntry modifier,
            Player player,
            InteractionHand hand,
            InteractionSource source
    ) {
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        if (source != InteractionSource.RIGHT_CLICK || player.isCrouching()) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        ModDataNBT data = tool.getPersistentData();

        if (!data.contains(getResource(RS_BOUND_POS))) {
            player.displayClientMessage(Component.translatable(MSG_NOT_BOUND), true);
            return InteractionResult.FAIL;
        }

        int slotIndex = hand == InteractionHand.MAIN_HAND
                ? player.getInventory().selected
                : 40;

        openWirelessGrid(serverPlayer, player.getItemInHand(hand), slotIndex);

        return InteractionResult.SUCCESS;
    }

    public static void openWirelessGrid(ServerPlayer player, ItemStack tinkerTool, int slotIndex) {
        ToolStack toolStack = ToolStack.from(tinkerTool);
        ModDataNBT tinkerData = toolStack.getPersistentData();

        if (!tinkerData.contains(getResource(RS_BOUND_POS))) {
            player.displayClientMessage(Component.translatable(MSG_NOT_BOUND), true);
            return;
        }

        CompoundTag posData = tinkerData.getCompound(getResource(RS_BOUND_POS));

        if (!posData.contains("pos", Tag.TAG_LONG) || !posData.contains("dim", Tag.TAG_STRING)) {
            player.displayClientMessage(Component.translatable(MSG_BOUND_DATA_BROKEN), true);
            return;
        }

        BlockPos pos = BlockPos.of(posData.getLong("pos"));
        ResourceLocation dimId = ResourceLocation.parse(posData.getString("dim"));

        ServerLevel targetLevel = player.server.getLevel(
                ResourceKey.create(Registries.DIMENSION, dimId)
        );

        if (targetLevel == null) {
            player.displayClientMessage(Component.translatable(MSG_DIMENSION_MISSING), true);
            return;
        }

        if (!ChunkTicketEvents.acquire(player, targetLevel, pos)) {
            player.displayClientMessage(Component.translatable(MSG_CHUNK_LOAD_FAILED), true);
            return;
        }

        INetwork network = findNetwork(targetLevel, pos);

        if (network == null) {
            ChunkTicketEvents.release(player.server, player.getUUID());
            player.displayClientMessage(Component.translatable(MSG_NETWORK_MISSING), true);
            return;
        }

        if (!network.getSecurityManager().hasPermission(Permission.MODIFY, player)) {
            ChunkTicketEvents.release(player.server, player.getUUID());
            player.displayClientMessage(Component.translatable(MSG_NO_PERMISSION), true);
            return;
        }

        ItemStack virtualGridStack = makeVirtualGridStack(tinkerTool, pos, dimId);

        AbstractContainerMenu beforeOpen = player.containerMenu;

        API.instance().getGridManager().openGrid(
                RSA_WIRELESS_CRAFTING_GRID,
                player,
                virtualGridStack,
                new PlayerSlot(slotIndex)
        );

        if (player.containerMenu == beforeOpen) {
            ChunkTicketEvents.release(player.server, player.getUUID());
            player.displayClientMessage(Component.translatable(MSG_OPEN_FAILED), true);
        } else {
            network.getItemStorageCache().flush();
        }
    }

    private static INetwork findNetwork(ServerLevel level, BlockPos pos) {
        var node = API.instance()
                .getNetworkNodeManager(level)
                .getNode(pos);

        if (node != null) {
            return node.getNetwork();
        }

        return API.instance()
                .getNetworkManager(level)
                .getNetwork(pos);
    }

    private static ItemStack makeVirtualGridStack(ItemStack tinkerTool, BlockPos pos, ResourceLocation dimId) {
        CompoundTag toolRoot = tinkerTool.getOrCreateTag();

        CompoundTag gridTag;

        if (toolRoot.contains(RS_GRID_STATE, Tag.TAG_COMPOUND)) {
            gridTag = toolRoot.getCompound(RS_GRID_STATE);
        } else {
            gridTag = new CompoundTag();
            toolRoot.put(RS_GRID_STATE, gridTag);
        }

        gridTag.putString("Dimension", dimId.toString());
        gridTag.putInt("NodeX", pos.getX());
        gridTag.putInt("NodeY", pos.getY());
        gridTag.putInt("NodeZ", pos.getZ());

        gridTag.putBoolean(IS_TINKER_PROXY, true);

        ItemStack virtual = tinkerTool.copy();
        virtual.setCount(1);
        virtual.setTag(gridTag);

        return virtual;
    }

    public static class ChunkTicketEvents {
        private static final Map<UUID, ActiveTicket> ACTIVE_TICKETS = new ConcurrentHashMap<>();

        private record ActiveTicket(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        }

        public static boolean acquire(ServerPlayer player, ServerLevel level, BlockPos boundPos) {
            release(player.server, player.getUUID());

            ChunkPos chunkPos = new ChunkPos(boundPos);

            boolean success = ForgeChunkManager.forceChunk(
                    level,
                    MOD_ID,
                    player.getUUID(),
                    chunkPos.x,
                    chunkPos.z,
                    true,
                    true
            );

            if (!success) {
                return false;
            }

            ACTIVE_TICKETS.put(
                    player.getUUID(),
                    new ActiveTicket(level.dimension(), chunkPos.x, chunkPos.z)
            );

            return true;
        }

        public static void release(MinecraftServer server, UUID playerId) {
            ActiveTicket ticket = ACTIVE_TICKETS.remove(playerId);

            if (ticket == null) {
                return;
            }

            ServerLevel level = server.getLevel(ticket.dimension());

            if (level == null) {
                return;
            }

            ForgeChunkManager.forceChunk(
                    level,
                    MOD_ID,
                    playerId,
                    ticket.chunkX(),
                    ticket.chunkZ(),
                    false,
                    true
            );
        }

        @SubscribeEvent
        public static void onContainerClose(PlayerContainerEvent.Close event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            if (!(event.getContainer() instanceof GridContainerMenu gridContainer)) {
                return;
            }

            if (!(gridContainer.getGrid() instanceof WirelessGrid wirelessGrid)) {
                return;
            }

            ItemStack stack = wirelessGrid.getStack();

            if (!stack.hasTag() || !stack.getOrCreateTag().getBoolean(IS_TINKER_PROXY)) {
                return;
            }

            MinecraftServer server = player.server;
            UUID playerId = player.getUUID();

            server.execute(() -> release(server, playerId));
        }

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            release(player.server, player.getUUID());
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            MinecraftServer server = event.getServer();

            for (UUID playerId : ACTIVE_TICKETS.keySet()) {
                release(server, playerId);
            }

            ACTIVE_TICKETS.clear();
        }
    }
}