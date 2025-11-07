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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

    @Mod.EventBusSubscriber
    public class SleepInstant extends NoLevelsModifier implements GeneralInteractionModifierHook {

        /** 玩家对应的虚拟床 */
        private static final Map<UUID, BlockPos> SLEEP_BEDS = new HashMap<>();
        /** 睡眠计时器 */
        private static final Map<UUID, Integer> SLEEP_TICKS = new HashMap<>();

        @Override
        public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player,
                                           InteractionHand hand, InteractionSource source) {
            if (player.level().isClientSide) return InteractionResult.PASS;
            Level level = player.level();

            if (source == InteractionSource.RIGHT_CLICK && player.isCrouching() && !tool.isBroken()) {
                if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

                // 白天禁止睡觉
                if (level.isDay()) {
                    player.sendSystemMessage(Component.literal("§7你现在睡不着……"));
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.6f, 1.0f);
                    return InteractionResult.FAIL;
                }

                // 创建虚拟床
                BlockPos bedPos = player.blockPosition();
                BlockState bed = Blocks.RED_BED.defaultBlockState()
                        .setValue(BedBlock.FACING, Direction.NORTH)
                        .setValue(BedBlock.PART, BedPart.HEAD)
                        .setValue(BedBlock.OCCUPIED, false);

                level.setBlockAndUpdate(bedPos, bed);
                SLEEP_BEDS.put(player.getUUID(), bedPos);

                // 尝试入睡
                var result = serverPlayer.startSleepInBed(bedPos);
                if (result != null && result.left().isPresent()) {
                    player.sendSystemMessage(Component.literal("§7" + result.left().get().getMessage().getString()));
                    level.removeBlock(bedPos, false);
                    SLEEP_BEDS.remove(player.getUUID());
                    return InteractionResult.FAIL;
                }

                // ⚠ 关键：清除床设置的重生点
                serverPlayer.setRespawnPosition(serverPlayer.level().dimension(), null, 0.0F, false, false);

                // 播放音效 + 提示
                player.sendSystemMessage(Component.literal("§b你慢慢进入了梦乡……"));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.8f, 1.0f);
                level.gameEvent(player, GameEvent.ENTITY_INTERACT, bedPos);

                // 记录睡眠开始计时（5 秒后触发白天）
                SLEEP_TICKS.put(player.getUUID(), 0);

                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        }

        /** 每 tick 检查睡眠进度 */
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            for (var entry : new HashMap<>(SLEEP_TICKS).entrySet()) {
                UUID uuid = entry.getKey();
                int tick = entry.getValue() + 1;
                SLEEP_TICKS.put(uuid, tick);

                if (tick >= 100) { // 5 秒后
                    SLEEP_TICKS.remove(uuid);
                    ServerPlayer player = event.getServer().getPlayerList().getPlayer(uuid);
                    if (player == null) continue;
                    ServerLevel level = player.serverLevel();

                    // 所有玩家都在睡觉则跳过夜晚
                    if (level.players().stream().allMatch(Player::isSleeping)) {
                        level.setDayTime(0);
                        level.setWeatherParameters(12000, 0, false, false);
                        level.players().forEach(p -> {
                            p.stopSleeping();
                            p.sendSystemMessage(Component.literal("§a天亮了！你感觉神清气爽！"));

                            // 删除虚拟床
                            BlockPos bedPos = SLEEP_BEDS.remove(p.getUUID());
                            if (bedPos != null && level.getBlockState(bedPos).getBlock() instanceof BedBlock) {
                                level.removeBlock(bedPos, false);
                            }
                        });
                    }
                }
            }
        }

        /** 玩家自然醒也清理床 */
        @SubscribeEvent
        public static void onWake(PlayerWakeUpEvent event) {
            Player player = event.getEntity();
            Level level = player.level();

            BlockPos bedPos = SLEEP_BEDS.remove(player.getUUID());
            if (bedPos != null && level.getBlockState(bedPos).getBlock() instanceof BedBlock) {
                level.removeBlock(bedPos, false);
            }
            SLEEP_TICKS.remove(player.getUUID());
        }

        @Override
        protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
            hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        }
    }