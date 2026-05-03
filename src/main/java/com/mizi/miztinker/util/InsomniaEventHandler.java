package com.mizi.miztinker.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;


@Mod.EventBusSubscriber(modid = "miztinker")
public class InsomniaEventHandler {

    private static final float ACCELERATION_PER_DAY = 0.5f;
    private static final float MAX_ACCELERATION = 500f;
    private static final ModifierId INSOMNIA_ID = new ModifierId("miztinker", "insomnia");
    private static final String NBT_INSOMNIA_BACKUP = "miztinker_insomnia_ticks";

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        float serverWideExtraTicks = 0f;
        boolean hasAnyInsomniac = false;

        for (ServerPlayer player : serverLevel.players()) {
            int level = getTotalInsomniaLevel(player);
            if (level > 0) {
                int ticksSinceRest = player.getStats().getValue(Stats.CUSTOM, Stats.TIME_SINCE_REST);
                float daysSinceRest = ticksSinceRest / 24000f;

                float currentAcceleration = Math.min(daysSinceRest * ACCELERATION_PER_DAY * level, MAX_ACCELERATION);

                if (currentAcceleration >= 0.1f) {
                    hasAnyInsomniac = true;
                    serverWideExtraTicks += currentAcceleration;

                    applyLocalRandomTickAcceleration(serverLevel, player, currentAcceleration);
                }
            }
        }

        if (hasAnyInsomniac && serverWideExtraTicks > 0) {
            long timeAdvance = (long) Math.min(serverWideExtraTicks, MAX_ACCELERATION);
            serverLevel.setDayTime(serverLevel.getDayTime() + timeAdvance);
        }
    }

    private static void applyLocalRandomTickAcceleration(ServerLevel level, ServerPlayer player, float power) {
        BlockPos center = player.blockPosition();
        int extraAttempts = (int) power;

        for (BlockPos targetPos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(targetPos);

            if (state.isRandomlyTicking()) {
                for (int i = 0; i < extraAttempts; i++) {
                    state.randomTick(level, targetPos, level.random);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int ticksSinceRest = player.getStats().getValue(Stats.CUSTOM, Stats.TIME_SINCE_REST);
            player.getPersistentData().putInt(NBT_INSOMNIA_BACKUP, ticksSinceRest);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag data = player.getPersistentData();
            if (data.contains(NBT_INSOMNIA_BACKUP)) {
                int backupTicks = data.getInt(NBT_INSOMNIA_BACKUP);
                player.getStats().setValue(player, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), backupTicks);
                data.remove(NBT_INSOMNIA_BACKUP);
            }
        }
    }

    private static int getTotalInsomniaLevel(ServerPlayer player) {
        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                total += ModifierUtil.getModifierLevel(stack, INSOMNIA_ID);
            }
        }
        return total;
    }
}