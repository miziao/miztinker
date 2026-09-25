package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.isFromDummmmmmyMod;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.modifierCutting;

public class FinalReturn extends NoLevelsModifier implements MeleeHitModifierHook, InventoryTickModifierHook, TooltipModifierHook {

    private static final long MAX_PROGRESS = 720000L;

    private static final String SAVE_NAME = "miztinker_final_return";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_FULL_REACHED_GAME_TIME = "FullReachedGameTime";

    private static final String LANG_TOOLTIP_PROGRESS = "modifier.miztinker.final_return.tooltip.progress";
    private static final String LANG_TOOLTIP_REMAINING = "modifier.miztinker.final_return.tooltip.remaining";
    private static final String LANG_TOOLTIP_FULL = "modifier.miztinker.final_return.tooltip.full";
    private static final String LANG_TOOLTIP_UNKNOWN = "modifier.miztinker.final_return.tooltip.unknown";

    private static final Map<UUID, Long> LAST_COUNTED_PLAYER_TICK = new ConcurrentHashMap<>();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public void onInventoryTick(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull Level level,
            @NotNull LivingEntity holder,
            int itemSlot,
            boolean isSelected,
            boolean isCorrectSlot,
            @NotNull ItemStack stack
    ) {
        if (level.isClientSide) {
            return;
        }

        if (!(holder instanceof Player player)) {
            return;
        }

        MinecraftServer server = level.getServer();

        if (server == null) {
            return;
        }

        long gameTime = level.getGameTime();
        UUID uuid = player.getUUID();

        Long lastTick = LAST_COUNTED_PLAYER_TICK.get(uuid);

        if (lastTick != null && lastTick == gameTime) {
            return;
        }

        LAST_COUNTED_PLAYER_TICK.put(uuid, gameTime);

        FinalReturnData data = FinalReturnData.get(server);

        if (data.resetIfFullAndNotUsed(gameTime)) {
            return;
        }

        data.addProgress(1L, gameTime);
    }

    @Override
    public void afterMeleeHit(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float damageDealt
    ) {
        applyFinalReturn(context);
    }

    @Override
    public void failedMeleeHit(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @NotNull ToolAttackContext context,
            float damageAttempted
    ) {
        applyFinalReturn(context);
    }

    private static void applyFinalReturn(ToolAttackContext context) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();

        if (target == null || player == null) {
            return;
        }

        if (player.level().isClientSide || target.level().isClientSide) {
            return;
        }

        if (target.getHealth() <= 0.0F || target.isDeadOrDying()) {
            return;
        }

        if (isFromDummmmmmyMod(target)) {
            return;
        }

        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        FinalReturnData data = FinalReturnData.get(server);

        long progressTicks = data.getProgress();

        if (progressTicks <= 0L) {
            return;
        }

        float progressPercent = data.getProgressPercent();

        if (progressPercent <= 0.0F) {
            return;
        }

        float maxHealth = Math.max(1.0F, target.getMaxHealth());
        float firstHitCompensation = 1.0F / maxHealth;
        float valueForCutting = progressPercent - 0.01F - firstHitCompensation;

        modifierCutting(
                target,
                player,
                maxHealth,
                valueForCutting
        );

        if (progressTicks >= MAX_PROGRESS) {
            data.resetProgress();
        }
    }

    @Override
    public void addTooltip(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            @Nullable Player player,
            @NotNull List<Component> tooltip,
            @NotNull TooltipKey tooltipKey,
            @NotNull TooltipFlag tooltipFlag
    ) {
        MinecraftServer server = getTooltipServer(player);

        if (server == null) {
            tooltip.add(Component.translatable(LANG_TOOLTIP_UNKNOWN));
            return;
        }

        FinalReturnData data = FinalReturnData.get(server);

        long progress = data.getProgress();
        float progressPercent = data.getProgressPercent();
        float remainingPercent = Math.max(0.0F, 1.0F - progressPercent);

        if (progress >= MAX_PROGRESS) {
            tooltip.add(Component.translatable(LANG_TOOLTIP_FULL));
            return;
        }

        tooltip.add(Component.translatable(
                LANG_TOOLTIP_PROGRESS,
                formatPercent(progressPercent)
        ));

        tooltip.add(Component.translatable(
                LANG_TOOLTIP_REMAINING,
                formatPercent(remainingPercent)
        ));
    }

    private static MinecraftServer getTooltipServer(@Nullable Player player) {
        if (player != null) {
            MinecraftServer server = player.getServer();

            if (server != null) {
                return server;
            }
        }

        return ServerLifecycleHooks.getCurrentServer();
    }

    private static String formatPercent(float value) {
        return String.format(Locale.ROOT, "%.2f", value * 100.0F);
    }

    public static class FinalReturnData extends SavedData {

        private long progress;
        private long fullReachedGameTime;

        public FinalReturnData() {
            this.progress = 0L;
            this.fullReachedGameTime = -1L;
        }

        public static FinalReturnData get(MinecraftServer server) {
            return server.overworld()
                    .getDataStorage()
                    .computeIfAbsent(
                            FinalReturnData::load,
                            FinalReturnData::new,
                            SAVE_NAME
                    );
        }

        public static FinalReturnData load(CompoundTag tag) {
            FinalReturnData data = new FinalReturnData();

            long savedProgress = tag.getLong(TAG_PROGRESS);
            data.progress = Math.max(0L, Math.min(MAX_PROGRESS, savedProgress));
            data.fullReachedGameTime = tag.getLong(TAG_FULL_REACHED_GAME_TIME);

            if (data.progress < MAX_PROGRESS) {
                data.fullReachedGameTime = -1L;
            }

            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putLong(TAG_PROGRESS, getProgress());
            tag.putLong(TAG_FULL_REACHED_GAME_TIME, this.fullReachedGameTime);
            return tag;
        }

        public long getProgress() {
            return Math.max(0L, Math.min(MAX_PROGRESS, this.progress));
        }

        public float getProgressPercent() {
            return Math.min(1.0F, getProgress() / (float) MAX_PROGRESS);
        }

        public void addProgress(long amount, long gameTime) {
            if (amount <= 0L) {
                return;
            }

            if (this.progress >= MAX_PROGRESS) {
                if (this.fullReachedGameTime < 0L) {
                    this.fullReachedGameTime = gameTime;
                    this.setDirty();
                }

                return;
            }

            this.progress = Math.min(MAX_PROGRESS, this.progress + amount);

            if (this.progress >= MAX_PROGRESS) {
                this.fullReachedGameTime = gameTime;
            } else {
                this.fullReachedGameTime = -1L;
            }

            this.setDirty();
        }


        public boolean resetIfFullAndNotUsed(long gameTime) {
            if (this.progress < MAX_PROGRESS) {
                return false;
            }

            if (this.fullReachedGameTime < 0L) {
                this.fullReachedGameTime = gameTime;
                this.setDirty();
                return false;
            }

            if (gameTime > this.fullReachedGameTime) {
                resetProgress();
                return true;
            }

            return false;
        }

        public void resetProgress() {
            this.progress = 0L;
            this.fullReachedGameTime = -1L;
            this.setDirty();
        }
    }
}