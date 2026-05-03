package com.mizi.miztinker.recipes.rules.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class NetherReactorRule implements IBlockInteractRule {
    private static final ResourceLocation ID = ResourceLocation.parse("miztinker:nether_reactor_interaction");

    @Override
    public boolean matches(BlockState state, ResourceLocation blockId, ItemStack heldStack, ResourceLocation heldId) {
        return heldStack.isEmpty() && ResourceLocation.parse("miztinker:nether_reactor").equals(blockId);
    }

    @Override
    public void execute(Player player, Level level, BlockPos pos, ItemStack heldStack) {
        if (!(level instanceof ServerLevel)) return;
        if (player.getServer() == null) return;

        ServerLevel nether = player.getServer().getLevel(Level.NETHER);

        if (nether == null) {
            ServerLevel overworld = player.getServer().overworld();
            BlockPos spawn = overworld.getSharedSpawnPos();
            teleportAction(player, overworld, spawn.getX(), spawn.getY(), spawn.getZ());

            player.displayClientMessage(Component.translatable("miztinker.nether_reactor.error.no_dimension"), true);
            return;
        }

        double targetX = player.getX() / 8.0;
        double targetZ = player.getZ() / 8.0;
        double targetY = player.getY();

        teleportAction(player, nether, targetX, targetY, targetZ);

        nether.sendParticles(ParticleTypes.LAVA, targetX, targetY, targetZ, 20, 0.5, 0.5, 0.5, 0.02);
        nether.sendParticles(ParticleTypes.FLAME, targetX, targetY, targetZ, 20, 0.5, 0.5, 0.5, 0.05);
        nether.playSound(null, targetX, targetY, targetZ, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 1.0f, 1.0f);
    }


    private void teleportAction(Player player, ServerLevel destWorld, double x, double y, double z) {
        player.changeDimension(destWorld, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                                      float yaw, Function<Boolean, Entity> repositionEntity) {
                Entity e = repositionEntity.apply(false);
                e.teleportTo(x, y, z);
                return e;
            }
        });
    }

    @Override
    public ResourceLocation getId() { return ID; }
}