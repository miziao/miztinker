package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class SleepInstant extends NoLevelsModifier implements GeneralInteractionModifierHook {

    private static final Map<UUID, BlockPos> SLEEP_BEDS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SLEEP_TICKS = new ConcurrentHashMap<>();

    @Override
    public @NotNull InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player,
                                                InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide) return InteractionResult.PASS;

        if (source == InteractionSource.RIGHT_CLICK && player.isCrouching() && !tool.isBroken()) {
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            ServerLevel level = serverPlayer.serverLevel();

            if (level.isDay()) {
                player.sendSystemMessage(Component.translatable("miztinker.modifier.sleep_instant.not_now"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.6f, 1.0f);
                return InteractionResult.FAIL;
            }

            BlockPos bedPos = player.blockPosition();
            if (!level.getBlockState(bedPos).canBeReplaced()) {
                bedPos = bedPos.above();
            }

            BlockState bed = Blocks.RED_BED.defaultBlockState()
                    .setValue(BedBlock.FACING, Direction.NORTH)
                    .setValue(BedBlock.PART, BedPart.HEAD)
                    .setValue(BedBlock.OCCUPIED, false);

            level.setBlockAndUpdate(bedPos, bed);
            SLEEP_BEDS.put(player.getUUID(), bedPos);

            var result = serverPlayer.startSleepInBed(bedPos);

            if (result.left().isPresent()) {
                Player.BedSleepingProblem problem = result.left().get();
                Component message = problem.getMessage();
                if (message != null) {
                    player.sendSystemMessage(message);
                }

                level.removeBlock(bedPos, false);
                SLEEP_BEDS.remove(player.getUUID());
                return InteractionResult.FAIL;
            }

            serverPlayer.setRespawnPosition(level.dimension(), null, 0.0F, false, false);

            player.sendSystemMessage(Component.translatable("miztinker.modifier.sleep_instant.start"));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.8f, 1.0f);
            level.gameEvent(player, GameEvent.ENTITY_INTERACT, bedPos);

            SLEEP_TICKS.put(player.getUUID(), 0);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        ServerLevel level = (ServerLevel) event.level;

        List<UUID> toRemove = new ArrayList<>();

        for (UUID uuid : SLEEP_TICKS.keySet()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
            if (player == null || player.level() != level || !player.isSleeping()) {
                toRemove.add(uuid);
                continue;
            }

            int tick = SLEEP_TICKS.get(uuid) + 1;
            SLEEP_TICKS.put(uuid, tick);

            if (tick >= 100) {
                boolean allReady = level.players().stream()
                        .filter(Player::isSleeping)
                        .allMatch(p -> SLEEP_TICKS.getOrDefault(p.getUUID(), 0) >= 100);

                if (allReady) {
                    level.setDayTime(0);
                    if (level.isRaining() || level.isThundering()) {
                        level.setWeatherParameters(12000, 0, false, false);
                    }

                    level.players().forEach(p -> {
                        if (p.isSleeping()) {
                            p.stopSleeping();
                            p.sendSystemMessage(Component.translatable("miztinker.modifier.sleep_instant.wake_up"));
                            cleanUpBed(p, level);
                        }
                    });
                    SLEEP_TICKS.clear();
                    break;
                }
            }
        }
        toRemove.forEach(SLEEP_TICKS::remove);
    }

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            cleanUpBed(event.getEntity(), level);
        }
    }

    private static void cleanUpBed(Player player, ServerLevel level) {
        BlockPos bedPos = SLEEP_BEDS.remove(player.getUUID());
        if (bedPos != null) {
            if (level.getBlockState(bedPos).is(Blocks.RED_BED)) {
                level.removeBlock(bedPos, false);
            }
        }
        SLEEP_TICKS.remove(player.getUUID());
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }
}