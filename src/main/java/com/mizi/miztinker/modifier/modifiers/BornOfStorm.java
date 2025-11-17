package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.WeatherChangePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.List;

/**
 * 匠魂特性：于暴风雨中诞生
 * - 空手点击物品栏工具切换天气：晴天 -> 雨天 -> 雷暴 -> 晴天
 * - Tooltip 显示当前天气状态
 */
public class BornOfStorm extends NoLevelsModifier implements SlotStackModifierHook, TooltipModifierHook {

    private static final ResourceLocation WEATHER_KEY = new ResourceLocation("miztinker", "bornofstorm.weather");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    /** 空手点击物品栏工具触发 */
    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot, Player player, SlotAccess access) {
        if (!held.isEmpty()) return false;
        if (slot.container != player.getInventory()) return false;

        ModDataNBT data = tool.getPersistentData();
        int current = data.getInt(WEATHER_KEY);
        int next = (current + 1) % 3;
        data.putInt(WEATHER_KEY, next);

        // ✅ 客户端发包同步到服务端
        if (player.level().isClientSide) {
            MiztinkerNetwork.CHANNEL.send(PacketDistributor.SERVER.noArg(), new WeatherChangePacket(next));
        }

        // 客户端即时显示提示
        String msg = switch (next) {
            case 1 -> "§b现在是§3雨天§b。";
            case 2 -> "§9现在是§1暴风雨§9！";
            default -> "§e现在是§6晴天§e。";
        };
        player.displayClientMessage(Component.literal(msg), true);

        return true;
    }

    /** Tooltip 显示当前天气状态 */
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player,
                           List<Component> tooltip, TooltipKey key, net.minecraft.world.item.TooltipFlag flag) {

        int state = tool.getPersistentData().getInt(WEATHER_KEY);

        // 优先根据世界天气显示
        if (player != null) {
            Level level = player.level();
            if (level.isRaining()) state = level.isThundering() ? 2 : 1;
            else state = 0;
        }

        String status = switch (state) {
            case 1 -> "§b当前天气：雨天";
            case 2 -> "§9当前天气：暴风雨";
            default -> "§e当前天气：晴天";
        };

        tooltip.add(Component.literal(status).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("空手右键切换天气模式").withStyle(ChatFormatting.DARK_GRAY));
    }
}