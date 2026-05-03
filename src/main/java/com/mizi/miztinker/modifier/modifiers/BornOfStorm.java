package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.WeatherChangePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class BornOfStorm extends NoLevelsModifier implements SlotStackModifierHook, TooltipModifierHook {

    private static final ResourceLocation WEATHER_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "bornofstorm.weather");

    private static final String KEY_STATUS_SUNNY = "modifier.miztinker.born_of_storm.sunny";
    private static final String KEY_STATUS_RAIN = "modifier.miztinker.born_of_storm.rain";
    private static final String KEY_STATUS_STORM = "modifier.miztinker.born_of_storm.storm";
    private static final String KEY_ACTION_HINT = "modifier.miztinker.born_of_storm.hint";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot, Player player, SlotAccess access) {
        if (!held.isEmpty()) return false;
        if (slot.container != player.getInventory()) return false;

        int current = tool.getPersistentData().getInt(WEATHER_KEY);
        int next = (current + 1) % 3;

        tool.getPersistentData().putInt(WEATHER_KEY, next);

        if (player.level().isClientSide) {
            MiztinkerNetwork.INSTANCE.send(PacketDistributor.SERVER.noArg(),
                    new WeatherChangePacket(next, slot.index));

            String msgKey = switch (next) {
                case 1 -> KEY_STATUS_RAIN;
                case 2 -> KEY_STATUS_STORM;
                default -> KEY_STATUS_SUNNY;
            };
            player.displayClientMessage(Component.translatable(msgKey), true);
        }

        return true;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player,
                           List<Component> tooltip, TooltipKey key, net.minecraft.world.item.TooltipFlag flag) {

        int state = 0;
        if (player != null) {
            Level level = player.level();
            if (level.isRaining()) {
                state = level.isThundering() ? 2 : 1;
            }
        } else {
            state = tool.getPersistentData().getInt(WEATHER_KEY);
        }

        String statusKey = switch (state) {
            case 1 -> KEY_STATUS_RAIN;
            case 2 -> KEY_STATUS_STORM;
            default -> KEY_STATUS_SUNNY;
        };

        tooltip.add(Component.translatable(statusKey).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(KEY_ACTION_HINT).withStyle(ChatFormatting.DARK_GRAY));
    }
}