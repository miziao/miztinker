package com.mizi.miztinker.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Function;

public class DiamondPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final ResourceLocation DIM_LOCATION = ResourceLocation.fromNamespaceAndPath("miztinker", "diamond_continent");

    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    public DiamondPortalBlock() {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .strength(-1.0F)
                .lightLevel((state) -> 12)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        Direction.Axis portalAxis = state.getValue(AXIS);
        Direction.Axis facingAxis = facing.getAxis();

        boolean isParallel = portalAxis != facingAxis && facingAxis.isHorizontal();
        if (!isParallel && !facingState.is(this) && !isCompleteStructure(level, currentPos, portalAxis)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    private boolean isCompleteStructure(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        return isValidFrame(level, pos.above()) &&
                isValidFrame(level, pos.below()) &&
                isValidFrame(level, pos.relative(axis == Direction.Axis.X ? Direction.EAST : Direction.NORTH)) &&
                isValidFrame(level, pos.relative(axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH));
    }

    private boolean isValidFrame(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE) || state.is(this)) return true;

        var fluid = level.getFluidState(pos);
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid.getType());
        return id != null && id.getPath().contains("molten_diamond");
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canChangeDimensions()) {
            entity.handleInsidePortal(pos);

            if (!level.isClientSide && !entity.isOnPortalCooldown()) {
                executeTeleport(entity, (ServerLevel) level);
            }
        }
    }

    private void executeTeleport(Entity entity, ServerLevel level) {
        ResourceLocation currentDim = level.dimension().location();
        ResourceKey<Level> dimKey = currentDim.equals(DIM_LOCATION)
                ? Level.OVERWORLD
                : ResourceKey.create(Registries.DIMENSION, DIM_LOCATION);

        ServerLevel dest = level.getServer().getLevel(dimKey);
        if (dest != null) {
            teleportEntity(entity, dest);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }
        for(int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return switch (rot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> state.getValue(AXIS) == Direction.Axis.X
                    ? state.setValue(AXIS, Direction.Axis.Z)
                    : state.setValue(AXIS, Direction.Axis.X);
            default -> state;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    private void teleportEntity(Entity entity, ServerLevel dest) {
        double targetX = entity.getX();
        double targetZ = entity.getZ();
        double targetY = dest.dimension().location().equals(DIM_LOCATION) ? 64.0 : entity.getY();

        entity.setPortalCooldown();
        entity.changeDimension(dest, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                Entity teleportedEntity = repositionEntity.apply(false);
                BlockPos playerPos = BlockPos.containing(targetX, targetY, targetZ);

                findExistingPortal(destWorld, playerPos, 32).ifPresentOrElse(
                        existingPos -> {
                            teleportedEntity.teleportTo(existingPos.getX() + 0.5, existingPos.getY(), existingPos.getZ() + 0.5);
                        },
                        () -> {
                            generatePortalStructure(destWorld, playerPos);
                            teleportedEntity.teleportTo(targetX + 0.5, targetY, targetZ + 0.5);
                        }
                );

                return teleportedEntity;
            }
        });
    }

    private Optional<BlockPos> findExistingPortal(ServerLevel level, BlockPos center, int radius) {
        return BlockPos.betweenClosedStream(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))
                .filter(p -> level.getBlockState(p).is(this))
                .map(BlockPos::immutable)
                .findFirst();
    }

    private void generatePortalStructure(ServerLevel level, BlockPos pos) {
        for (int x = -1; x <= 2; x++) {
            for (int y = -1; y <= 3; y++) {
                boolean isEdge = (x == -1 || x == 2 || y == -1 || y == 3);
                if (isEdge) {
                    BlockPos framePos = pos.offset(x, y, 0);
                    if (level.isEmptyBlock(framePos) || level.getBlockState(framePos).canBeReplaced()) {
                        level.setBlockAndUpdate(framePos, Blocks.DIAMOND_ORE.defaultBlockState());
                    }
                }
            }
        }

        for (int y = 0; y <= 2; y++) {
            for (int x = 0; x <= 1; x++) {
                BlockPos portalPos = pos.offset(x, y, 0);
                level.setBlock(portalPos, this.defaultBlockState().setValue(AXIS, Direction.Axis.X), 3);
            }
        }
    }
}