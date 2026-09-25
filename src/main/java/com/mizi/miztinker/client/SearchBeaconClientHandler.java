package com.mizi.miztinker.client;

import com.mizi.miztinker.modifier.modifiers.SearchBeacon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "miztinker")
public class SearchBeaconClientHandler {
    private static final int MAX_FOUND_POSITIONS = 200;
    private static final int MAX_RENDERED_BEAMS = 64;
    private static final int SCAN_HEIGHT_PER_TICK = 24;

    private static final Set<BlockPos> FOUND_POSITIONS = ConcurrentHashMap.newKeySet();
    private static int lastMinY = 0;
    private static BlockState lastTarget = null;
    private static boolean wasActive = false;


    private static final ModifierId SEARCH_BEACON_ID = new ModifierId("miztinker", "search_beacon");

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ToolStack tool = getHeldSearchTool(mc.player);
        if (tool == null) {
            if (wasActive) {
                FOUND_POSITIONS.clear();
                wasActive = false;
            }
            return;
        }

        CompoundTag data = tool.getPersistentData().getCompound(SearchBeacon.DATA_KEY);
        boolean isActive = data.getBoolean("is_active");

        if (isActive) {
            wasActive = true;
            BlockState target = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), data.getCompound("target_block"));

            if (lastTarget == null || !lastTarget.getBlock().equals(target.getBlock())) {
                FOUND_POSITIONS.clear();
                lastTarget = target;
                lastMinY = mc.level.getMinBuildHeight();
            }

            int expandedLevel = tool.getModifierLevel(TinkerModifiers.expanded.getId());
            runOptimizedScan(mc.player, mc.level, target, expandedLevel);

            if (!FOUND_POSITIONS.isEmpty()) {
                renderBeams(event, mc, mc.level, target);
            }
        } else if (wasActive) {
            FOUND_POSITIONS.clear();
            wasActive = false;
        }
    }

    private static void runOptimizedScan(Player player, Level level, BlockState target, int expandedLevel) {
        BlockPos center = player.blockPosition();
        int radius = (expandedLevel + 1) * 16;
        long maxCachedDistanceSqr = (long) radius * radius * 4L;

        FOUND_POSITIONS.removeIf(p -> {
            if (horizontalDistSqr(p, center) > maxCachedDistanceSqr) {
                return true;
            }
            if (level.isLoaded(p)) {
                return !level.getBlockState(p).is(target.getBlock());
            }
            return false;
        });

        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        if (lastMinY < minY || lastMinY >= maxY) {
            lastMinY = minY;
        }

        int currentMaxY = Math.min(lastMinY + SCAN_HEIGHT_PER_TICK, maxY);
        PriorityQueue<BlockPos> farthestFirst = new PriorityQueue<>(
                Comparator.comparingLong((BlockPos p) -> horizontalDistSqr(p, center)).reversed()
        );
        farthestFirst.addAll(FOUND_POSITIONS);

        for (int y = lastMinY; y < currentMaxY; y++) {
            for (int x = center.getX() - radius; x <= center.getX() + radius; x += 16) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z += 16) {
                    if (level.isLoaded(new BlockPos(x, y, z))) {
                        searchSubArea(level, x, z, y, target, center, maxCachedDistanceSqr, farthestFirst);
                    }
                }
            }
        }
        lastMinY = currentMaxY;
    }

    private static void searchSubArea(
            Level level,
            int startX,
            int startZ,
            int y,
            BlockState target,
            BlockPos center,
            long maxCachedDistanceSqr,
            PriorityQueue<BlockPos> farthestFirst
    ) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                BlockPos p = new BlockPos(startX + i, y, startZ + j);
                long distanceSqr = horizontalDistSqr(p, center);
                if (distanceSqr <= maxCachedDistanceSqr && level.getBlockState(p).is(target.getBlock())) {
                    addFoundPosition(p.immutable(), distanceSqr, center, farthestFirst);
                }
            }
        }
    }

    private static void addFoundPosition(
            BlockPos pos,
            long distanceSqr,
            BlockPos center,
            PriorityQueue<BlockPos> farthestFirst
    ) {
        if (FOUND_POSITIONS.contains(pos)) {
            return;
        }

        if (FOUND_POSITIONS.size() < MAX_FOUND_POSITIONS) {
            FOUND_POSITIONS.add(pos);
            farthestFirst.offer(pos);
            return;
        }

        BlockPos farthest = farthestFirst.peek();
        if (farthest != null && distanceSqr < horizontalDistSqr(farthest, center)) {
            FOUND_POSITIONS.remove(farthest);
            farthestFirst.poll();
            FOUND_POSITIONS.add(pos);
            farthestFirst.offer(pos);
        }
    }

    private static void renderBeams(RenderLevelStageEvent event, Minecraft mc, Level level, BlockState target) {
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        float[] colors = new float[]{1.0F, 1.0F, 1.0F};
        int colorInt = target.getMapColor(level, BlockPos.ZERO).col;
        if (colorInt != 0) {
            colors[0] = (float) (colorInt >> 16 & 255) / 255.0F;
            colors[1] = (float) (colorInt >> 8 & 255) / 255.0F;
            colors[2] = (float) (colorInt & 255) / 255.0F;
        }

        double camX = mc.getEntityRenderDispatcher().camera.getPosition().x;
        double camY = mc.getEntityRenderDispatcher().camera.getPosition().y;
        double camZ = mc.getEntityRenderDispatcher().camera.getPosition().z;

        int minY = level.getMinBuildHeight();
        int worldHeight = level.getHeight();

        BlockPos playerPos = null;
        if (mc.player != null) {
            playerPos = mc.player.blockPosition();
        }

        List<BlockPos> renderPositions = getNearestRenderPositions(playerPos);
        for (BlockPos pos : renderPositions) {

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);

            BeaconRenderer.renderBeaconBeam(
                    poseStack, bufferSource, BeaconRenderer.BEAM_LOCATION,
                    event.getPartialTick(), 1.0F, level.getGameTime(),
                    minY - pos.getY(),
                    worldHeight,
                    colors, 0.1F, 0.15F
            );
            poseStack.popPose();
        }
    }

    private static List<BlockPos> getNearestRenderPositions(BlockPos playerPos) {
        List<BlockPos> renderPositions = new ArrayList<>(FOUND_POSITIONS);

        if (playerPos != null) {
            renderPositions.sort(Comparator.comparingLong(p -> horizontalDistSqr(p, playerPos)));
        }

        if (renderPositions.size() > MAX_RENDERED_BEAMS) {
            return renderPositions.subList(0, MAX_RENDERED_BEAMS);
        }

        return renderPositions;
    }

    private static long horizontalDistSqr(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    private static ToolStack getHeldSearchTool(Player player) {
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty() && main.getOrCreateTag().contains("tic_modifiers")) {
            ToolStack tool = ToolStack.from(main);
            if (tool.getModifierLevel(SEARCH_BEACON_ID) > 0) return tool;
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty() && off.getOrCreateTag().contains("tic_modifiers")) {
            ToolStack tool = ToolStack.from(off);
            if (tool.getModifierLevel(SEARCH_BEACON_ID) > 0) return tool;
        }
        return null;
    }

    @SubscribeEvent
    public static void onRenderGui(net.minecraftforge.client.event.RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        ToolStack tool = getHeldSearchTool(mc.player);
        if (tool == null) return;

        CompoundTag data = tool.getPersistentData().getCompound(SearchBeacon.DATA_KEY);
        if (!data.getBoolean("is_active")) return;

        net.minecraft.client.gui.GuiGraphics graphics = event.getGuiGraphics();

        int expandedLevel = tool.getModifierLevel(TinkerModifiers.expanded.getId());
        int radius = (expandedLevel + 1) * 16;
        BlockState targetState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), data.getCompound("target_block"));
        String blockName = targetState.getBlock().getName().getString();
        int foundCount = FOUND_POSITIONS.size();

        int x = 12;
        int y = 12;
        int width = 130;
        int height = 45;
        int padding = 5;

        graphics.fill(x - padding, y - padding, x + width, y + height, 0x88000000);

        graphics.fill(x - padding, y - padding, x - padding + 1, y + height, 0xFF00E6E6);

        graphics.drawString(mc.font, net.minecraft.network.chat.Component.translatable("gui.miztinker.search_system.title"), x, y, 0xFFFFFF);


        graphics.drawString(mc.font, net.minecraft.network.chat.Component.translatable("gui.miztinker.search_system.target", blockName), x, y + 12, 0xCCCCCC);

        graphics.drawString(mc.font, net.minecraft.network.chat.Component.translatable("gui.miztinker.search_system.range", radius), x, y + 22, 0xCCCCCC);

        graphics.drawString(mc.font, net.minecraft.network.chat.Component.translatable("gui.miztinker.search_system.count", foundCount), x, y + 32, 0xCCCCCC);
    }
}
