package com.mizi.miztinker.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import java.util.Random;

public class TreeGeneratorTask {
    private final ServerLevel level;
    private final BlockPos root;
    private final int radius;
    private final Random random = new Random();

    private int currentX;
    private int currentZ;
    private boolean baseTrunkGenerated = false;
    @Getter
    private boolean finished = false;

    private static final int MAX_TRUNK_Y = 250;

    public TreeGeneratorTask(ServerLevel level, BlockPos root, int radius) {
        this.level = level;
        this.root = root;
        this.radius = radius;
        this.currentX = -radius;
        this.currentZ = -radius;
    }

    public void runTick() {
        if (finished) return;

        if (!baseTrunkGenerated) {
            generateComplexTrunk();
            baseTrunkGenerated = true;
            return;
        }

        int processed = 0;
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        BlockPos canopyBase = new BlockPos(root.getX(), 235, root.getZ());

        while (processed < 2000) {
            double distSq = (double)currentX * currentX + (double)currentZ * currentZ;
            double maxDistSq = (double)radius * radius;

            if (distSq <= maxDistSq) {
                double normalizedDist = Math.sqrt(distSq) / radius;

                int thickness = (int) ((1.0 - normalizedDist) * 20) + 5;

                for (int y = 0; y < thickness; y++) {
                    if (normalizedDist > 0.8 && random.nextFloat() > 0.5) continue;

                    BlockPos target = canopyBase.offset(currentX, y, currentZ);
                    if (target.getY() < 320 && level.hasChunkAt(target) && level.isEmptyBlock(target)) {
                        level.setBlock(target, leaves, 2);
                    }
                }
                processed++;
            }

            currentZ++;
            if (currentZ > radius) {
                currentZ = -radius;
                currentX++;
                if (currentX > radius) {
                    finished = true;
                    break;
                }
            }
        }
    }

    private void generateComplexTrunk() {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();

        int startY = root.getY();
        int totalHeight = MAX_TRUNK_Y - startY;

        for (int yOffset = 0; yOffset <= totalHeight; yOffset++) {
            BlockPos currentLevel = root.above(yOffset);

            int size = (yOffset < 20) ? 4 : (yOffset < 100 ? 3 : 2);

            for (int x = -size; x <= size; x++) {
                for (int z = -size; z <= size; z++) {
                    BlockPos logPos = currentLevel.offset(x, 0, z);
                    if (logPos.getY() < 256) {
                        level.setBlock(logPos, log, 2);
                    }
                }
            }

            if (yOffset > 100 && random.nextInt(6) == 0) {
                int branchLength = 20 + random.nextInt(30);
                generateBranch(currentLevel, random.nextInt(360), branchLength);
            }
        }
    }

    private void generateBranch(BlockPos start, int angle, int length) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        double rad = Math.toRadians(angle);
        double dx = Math.cos(rad);
        double dz = Math.sin(rad);
        double dy = 0.4;

        for (int i = 0; i < length; i++) {
            BlockPos branchPos = start.offset((int)(dx * i), (int)(dy * i), (int)(dz * i));
            if (branchPos.getY() < 310 && level.hasChunkAt(branchPos)) {
                level.setBlock(branchPos, log, 2);
                level.setBlock(branchPos.above(), log, 2);
            }
        }
    }
}