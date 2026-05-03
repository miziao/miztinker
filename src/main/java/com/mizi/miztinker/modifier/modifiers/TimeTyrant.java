package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.text.DecimalFormat;
import java.util.List;

public class TimeTyrant extends NoLevelsModifier implements SlotStackModifierHook, TooltipModifierHook {

    private static final ResourceLocation TIME_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "timetyant.time_state");
    private static final ResourceLocation COOLDOWN_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "timetyant.last_tick");
    private static final int COOLDOWN_TICKS = 20;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot, Player player, SlotAccess access) {
        if (!held.isEmpty()) return false;

        Level level = player.level();
        if (level.isClientSide) return true;

        ModDataNBT data = tool.getPersistentData();

        int currentTime = (int) level.getGameTime();
        int lastTime = data.getInt(COOLDOWN_KEY);

        if (currentTime - lastTime < COOLDOWN_TICKS && currentTime >= lastTime) {
            return true;
        }

        data.putInt(COOLDOWN_KEY, currentTime);

        int current = data.getInt(TIME_KEY);
        int next = (current + 1) % 4;
        data.putInt(TIME_KEY, next);

        if (level instanceof ServerLevel serverLevel && level.dimensionTypeId().equals(BuiltinDimensionTypes.OVERWORLD)) {
            long newTime = switch (next) {
                case 1 -> 6000L;
                case 2 -> 13000L;
                case 3 -> 18000L;
                default -> 0L;
            };
            serverLevel.setDayTime(newTime);
        }

        player.displayClientMessage(Component.translatable("modifier.miztinker.time_tyrant.state." + next), true);

        return true;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player,
                           List<Component> tooltip, TooltipKey key, net.minecraft.world.item.TooltipFlag flag) {

        if (player == null) return;
        Level level = player.level();

        if (level.dimensionTypeId().equals(BuiltinDimensionTypes.OVERWORLD)) {
            long time = level.getDayTime() % 24000;
            double hours = (time / 1000.0) + 6;
            if (hours >= 24) hours -= 24;
            double minutes = (time % 1000) / 1000.0 * 60.0;

            DecimalFormat df = new DecimalFormat("00");
            String timeStr = df.format((int) hours) + ":" + df.format((int) minutes);
            tooltip.add(Component.translatable("modifier.miztinker.time_tyrant.current_time", timeStr).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("modifier.miztinker.time_tyrant.not_overworld").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}