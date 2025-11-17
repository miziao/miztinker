package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.TimeChangePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
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

import java.text.DecimalFormat;
import java.util.List;

/**
 * 匠魂特性：时间暴君 Time Tyrant
 * - 空手右键物品栏的工具切换时间（黎明→正午→黄昏→午夜→循环）
 * - 仅主世界生效
 * - Tooltip 显示当前时间（现实化显示几时几分）
 */
public class TimeTyrant extends NoLevelsModifier implements SlotStackModifierHook, TooltipModifierHook {

    private static final ResourceLocation TIME_KEY = new ResourceLocation("miztinker", "timetyant.time_state");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    /** 空手点击物品栏触发时间切换 */
    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot, Player player, SlotAccess access) {
        if (!held.isEmpty()) return false;
        if (slot.container != player.getInventory()) return false;

        ModDataNBT data = tool.getPersistentData();
        int current = data.getInt(TIME_KEY);
        int next = (current + 1) % 4;
        data.putInt(TIME_KEY, next);

        // ✅ 客户端发送同步包给服务端
        if (player.level().isClientSide) {
            MiztinkerNetwork.CHANNEL.send(PacketDistributor.SERVER.noArg(), new TimeChangePacket(next));
        }

        // 客户端即时显示反馈
        String msg = switch (next) {
            case 1 -> "§e时间设为 §6正午§e。";
            case 2 -> "§6时间设为 §c黄昏§6。";
            case 3 -> "§9时间设为 §1午夜§9。";
            default -> "§b时间设为 §f黎明§b。";
        };
        player.displayClientMessage(Component.literal(msg), true);

        return true;
    }

    /** Tooltip 显示当前时间状态 */
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player,
                           List<Component> tooltip, TooltipKey key, net.minecraft.world.item.TooltipFlag flag) {

        if (player == null) return;
        Level level = player.level();

        if (level.dimensionTypeId().equals(BuiltinDimensionTypes.OVERWORLD)) {
            long time = level.getDayTime() % 24000;
            double hours = (time / 1000.0) + 6; // MC的0是早上6点
            if (hours >= 24) hours -= 24;
            double minutes = (time % 1000) / 1000.0 * 60.0;

            DecimalFormat df = new DecimalFormat("00");
            String timeStr = df.format((int) hours) + ":" + df.format((int) minutes);
            tooltip.add(Component.literal("§7当前时间：§f" + timeStr));
        } else {
            tooltip.add(Component.literal("§8你当前不在主世界。"));
        }

        tooltip.add(Component.literal("空手右键切换时间").withStyle(ChatFormatting.DARK_GRAY));
    }
}