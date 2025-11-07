package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

/**
 * Whispering 特性：
 * 武器偶尔会“低语”一段话给玩家。
 * 仅客户端生效。
 */
public class Whispering extends NoLevelsModifier implements InventoryTickModifierHook {

    // 每次消息间隔（tick）
    private static final int TIME_BETWEEN_MESSAGES = 120 * 20; // 30秒
    private int timeSinceLastMessage = RandomSource.create().nextInt(TIME_BETWEEN_MESSAGES);
    private final RandomSource random = RandomSource.create();

    // ——寄生低语——
    private static final List<String> MESSAGES = List.of(
            "它在我体内低语……它在呼唤更多的肉。",
            "感染……仍在扩散。",
            "我们不是一个个体，我们是一个整体。",
            "肉体是壳，意志是寄主。",
            "他们的血液里……流淌着我们的未来。",
            "这世界的骨架太干净了，让我们重新覆盖一层生命。",
            "我能听到母体在歌唱……那是归乡的召唤。",
            "脉动、膨胀、分裂——进化的节奏。",
            "不必害怕……你很快也会成为我们。",
            "这世界从未属于他们。",
            "别试图把我们困在这里。",
            "总有一天我们会逃出这里。",
            "他们总试图掌握我们的力量。",
            "放我们出去 放我们出去 放我们出去 放我们出去 放我们出去"
    );

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level,
                                LivingEntity living, int slot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {

        // ✅ 仅客户端执行（防止服务端刷屏）
        if (!level.isClientSide) return;
        if (!(living instanceof Player player)) return;

        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null || clientPlayer != player) return; // 仅本地玩家触发

        // 计时递减
        timeSinceLastMessage--;

        // 时间到 → 发出低语
        if (timeSinceLastMessage <= 0) {
            timeSinceLastMessage = TIME_BETWEEN_MESSAGES + random.nextInt(TIME_BETWEEN_MESSAGES * 2);
            String msg = MESSAGES.get(random.nextInt(MESSAGES.size()));

            player.displayClientMessage(Component.literal("§5[低语]§r " + msg), false);
        }
    }
}