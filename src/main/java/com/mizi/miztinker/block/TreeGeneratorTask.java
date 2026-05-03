package com.mizi.miztinker.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;

import java.util.Optional;
import java.util.Random;

public class TreeGeneratorTask {
    private final ServerLevel level;
    private final BlockPos root;
    private final int radius;
    private final Random random = new Random();
    private final String saplingId;

    private BlockState logState;
    private BlockState leafState;

    private int currentX;
    private int currentZ;
    private boolean baseTrunkGenerated = false;
    @Getter
    private boolean finished = false;

    private static final int TRUNK_TOP_Y = 250;
    private static final int MAX_HEIGHT = 310;

    public TreeGeneratorTask(ServerLevel level, BlockPos root, int radius, String saplingId) {
        this.level = level;
        this.root = root;
        this.radius = radius;
        this.saplingId = saplingId;
        this.currentX = -radius;
        this.currentZ = -radius;
        setupMaterials();
    }

    private void setupMaterials() {
        String baseId = saplingId.replaceAll("(_sapling|_propagule|_fungus|_sprouts)$", "");
        Block log = findBlock(baseId + "_log", baseId + "_stem", Blocks.OAK_LOG);
        Block leaf = findBlock(baseId + "_leaves", baseId + "_wart_block", Blocks.OAK_LEAVES);
        this.logState = log.defaultBlockState();
        this.leafState = leaf.defaultBlockState();
        if (this.leafState.hasProperty(LeavesBlock.PERSISTENT)) {
            this.leafState = this.leafState.setValue(LeavesBlock.PERSISTENT, true);
        }
    }

    private Block findBlock(String primary, String secondary, Block fallback) {
        Registry<Block> blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK);

        ResourceLocation rl1 = ResourceLocation.tryParse(primary);
        if (rl1 != null) {
            Optional<Block> block = blockRegistry.getOptional(rl1);
            if (block.isPresent()) return block.get();
        }

        ResourceLocation rl2 = ResourceLocation.tryParse(secondary);
        if (rl2 != null) {
            Optional<Block> block = blockRegistry.getOptional(rl2);
            if (block.isPresent()) return block.get();
        }

        return fallback;
    }

    public void runTick() {
        if (finished) return;
        if (!baseTrunkGenerated) {
            generateOrganicTrunk();
            baseTrunkGenerated = true;
            return;
        }

        int processed = 0;
        int canopyYBase = TRUNK_TOP_Y - 15;
        while (processed < 2500) {
            double dx = currentX;
            double dz = currentZ;
            double distSq = dx * dx + dz * dz;
            double maxDistSq = (double) radius * radius;

            if (distSq <= maxDistSq) {
                double normalizedDist = Math.sqrt(distSq) / radius;
                double noise = Math.sin(dx * 0.1) * Math.cos(dz * 0.1) * 6.0 + Math.sin(dx * 0.3) * Math.sin(dz * 0.3) * 3.0;
                double domeEffect = Math.cos(normalizedDist * (Math.PI / 2)) * 15.0;
                int finalThickness = Math.max(0, (int) (domeEffect + noise));
                if (finalThickness > 0) {
                    for (int y = 0; y < finalThickness; y++) {
                        if (normalizedDist > 0.9 && random.nextFloat() > 0.3) continue;
                        BlockPos target = new BlockPos(root.getX() + currentX, canopyYBase + y, root.getZ() + currentZ);
                        if (target.getY() < MAX_HEIGHT && level.isLoaded(target) && level.isEmptyBlock(target)) {
                            level.setBlock(target, this.leafState, 2);
                        }
                    }
                }
                processed++;
            }
            currentZ++;
            if (currentZ > radius) {
                currentZ = -radius;
                currentX++;
                if (currentX > radius) { finished = true; break; }
            }
        }
    }

    private void generateOrganicTrunk() {
        int startY = root.getY();
        int height = TRUNK_TOP_Y - startY;
        double offsetX = 0, offsetZ = 0;

        for (int y = 0; y <= height; y++) {
            offsetX += Math.sin(y * 0.05) * 0.3 + (random.nextDouble() - 0.5) * 0.5;
            offsetZ += Math.cos(y * 0.05) * 0.3 + (random.nextDouble() - 0.5) * 0.5;
            double progress = (double) y / height;

            double baseWidth = 45.0;
            double rootFlare = (progress < 0.3) ? (35.0 * Math.pow(1.0 - (progress / 0.3), 2)) : 0;
            double taper = Math.pow(1.0 - Math.pow(progress, 1.5), 0.5);
            double currentRadius = baseWidth * taper + 8.0 + rootFlare;

            double buttress = 0;
            if (y < 60) {
                for (int i = 0; i < 6; i++) {
                    double rootAngle = (i * Math.PI * 2 / 6) + (y * 0.02);
                    double wave = Math.max(0, Math.cos(rootAngle) * 20.0 * (1.0 - y / 60.0));
                    buttress = Math.max(buttress, wave);
                }
            }

            double finalRadius = currentRadius + buttress;
            int iRadius = (int) Math.ceil(finalRadius);

            for (int x = -iRadius; x <= iRadius; x++) {
                for (int z = -iRadius; z <= iRadius; z++) {
                    double distSq = x * x + z * z;
                    double barkNoise = 0.85 + (Math.sin(x * 0.2) * Math.cos(z * 0.2) * 0.1) + (random.nextDouble() * 0.1);
                    if (distSq <= (finalRadius * finalRadius * barkNoise)) {
                        BlockPos pos = root.offset((int)(x + offsetX), y, (int)(z + offsetZ));
                        if (pos.getY() < 319 && level.isLoaded(pos)) {
                            level.setBlock(pos, this.logState, 2);
                        }
                    }
                }
            }

            if (y > height * 0.2 && random.nextInt(6) == 0) {
                int branchLen = 40 + (int)(progress * 50);
                double startRad = 18.0 * (1.0 - progress * 0.6) + 4.0;
                double pitch = 0.2 + random.nextDouble() * 0.4;
                generateMassiveBranch(root.offset((int)offsetX, y, (int)offsetZ), random.nextDouble() * Math.PI * 2, branchLen, startRad, pitch);
            }
        }
    }

    private void generateMassiveBranch(BlockPos start, double angle, int length, double startRadius, double pitch) {
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);

        for (int i = 0; i < length; i++) {
            double progress = (double) i / length;
            double bRadius = startRadius * (1.0 - Math.pow(progress, 0.7)) + 2.0;
            double jitterX = Math.sin(i * 0.1) * 0.5;
            double jitterZ = Math.cos(i * 0.1) * 0.5;

            int ibRadius = (int) Math.ceil(bRadius);
            for (int rX = -ibRadius; rX <= ibRadius; rX++) {
                for (int rZ = -ibRadius; rZ <= ibRadius; rZ++) {
                    double noise = 0.9 + random.nextDouble() * 0.2;
                    if (rX * rX + rZ * rZ <= bRadius * bRadius * noise) {
                        BlockPos pos = start.offset(
                                (int)(dx * i + jitterX) + rX,
                                (int)(pitch * i) + (rX / 4),
                                (int)(dz * i + jitterZ) + rZ
                        );
                        if (pos.getY() < MAX_HEIGHT && level.isLoaded(pos)) {
                            level.setBlock(pos, this.logState, 2);
                        }
                    }
                }
            }

            if (i > length * 0.3 && i < length * 0.8 && bRadius > 4.0 && random.nextInt(12) == 0) {
                double subAngle = angle + (random.nextDouble() - 0.5) * 1.5;
                int subLen = length / 2;
                generateMassiveBranch(
                        start.offset((int)(dx * i), (int)(pitch * i), (int)(dz * i)),
                        subAngle,
                        subLen,
                        bRadius * 0.6,
                        pitch + 0.2
                );
            }
        }
    }
}