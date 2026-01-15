package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import static com.mizi.miztinker.miztinker.getResource;

public class Level_promotion extends NoLevelsModifier implements SlotStackModifierHook {

    public static final ResourceLocation TAG_DEATH_COUNT = getResource("death_count");
    public static final ResourceLocation KING_MODE_REVEALED = getResource("king_mode_revealed");

    private static final int REQUIRED_KILLS = 10_000;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry, ItemStack held, Slot slot, Player player, SlotAccess access) {

        ModDataNBT data = tool.getPersistentData();
        int kills = data.getInt(TAG_DEATH_COUNT);

        if (kills < REQUIRED_KILLS) {
            int remaining = REQUIRED_KILLS - kills;
            player.displayClientMessage(Component.literal(
                    String.format("§8死神大王冷冷地看向你：§7“这本笔记还需要 §4%d §7个生命才能填满。”", remaining)
            ), true);
            return true;
        }

        boolean isKing = !data.getBoolean(KING_MODE_REVEALED);
        data.putBoolean(KING_MODE_REVEALED, isKing);

        if (isKing) {
            player.displayClientMessage(Component.literal("§4死神大王微微点头，墨迹开始在纸张上疯狂蔓延..."), true);
        } else {
            player.displayClientMessage(Component.literal("§7死神的力量归于沉寂，形态重新封印。"), true);
        }

        return true;
    }
}