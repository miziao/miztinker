package com.mizi.miztinker.util;

import com.mizi.miztinker.block.DiamondPortalBlock;
import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static com.mizi.miztinker.miztinker.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class PortalFrameDetector {

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof Level level)) return;

        BlockPos pos = event.getPos();

        if (isMoltenDiamond(level, pos)) {
            if (attemptActivate(level, pos, Direction.Axis.X)) return;
            attemptActivate(level, pos, Direction.Axis.Z);
        }
    }

    private static boolean attemptActivate(Level level, BlockPos pos, Direction.Axis axis) {
        BlockPos bottomL = pos;

        int moveLimit = 21;
        while (moveLimit-- > 0 && isValidInternalSpace(level, bottomL.below())) {
            bottomL = bottomL.below();
        }

        moveLimit = 21;
        Direction leftDir = (axis == Direction.Axis.X) ? Direction.WEST : Direction.NORTH;
        while (moveLimit-- > 0 && isValidInternalSpace(level, bottomL.relative(leftDir))) {
            bottomL = bottomL.relative(leftDir);
        }

        if (level.getBlockState(bottomL).is(MiztinkerBlocks.DIAMOND_CONTINENT_PORTAL.get())) {
            return false;
        }

        int width = 2;
        int height = 3;

        for (int w = -1; w <= width; w++) {
            for (int h = -1; h <= height; h++) {
                BlockPos actualPos = (axis == Direction.Axis.X)
                        ? bottomL.offset(w, h, 0)
                        : bottomL.offset(0, h, w);

                boolean isFrame = (w == -1 || w == width || h == -1 || h == height);
                if (isFrame) {
                    if (!isDiamondFrame(level, actualPos)) return false;
                } else {
                    if (!isValidInternalSpace(level, actualPos)) return false;
                }
            }
        }

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                BlockPos target = (axis == Direction.Axis.X)
                        ? bottomL.offset(w, h, 0)
                        : bottomL.offset(0, h, w);

                level.setBlock(target, MiztinkerBlocks.DIAMOND_CONTINENT_PORTAL.get().defaultBlockState()
                        .setValue(DiamondPortalBlock.AXIS, axis), 2);
            }
        }

        level.playSound(null, bottomL, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private static boolean isMoltenDiamond(Level level, BlockPos pos) {
        var fluid = level.getFluidState(pos);
        if (fluid.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid.getType());
        return id != null && id.getPath().contains("molten_diamond");
    }

    private static boolean isValidInternalSpace(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(MiztinkerBlocks.DIAMOND_CONTINENT_PORTAL.get())) return false;
        if (state.isAir() || state.canBeReplaced()) return true;
        return isMoltenDiamond(level, pos);
    }

    private static boolean isDiamondFrame(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
    }
}