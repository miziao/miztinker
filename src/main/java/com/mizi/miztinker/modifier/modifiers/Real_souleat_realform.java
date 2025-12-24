package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import static com.mizi.miztinker.miztinker.getResource;
import static com.mizi.miztinker.modifier.modifiers.SoulEat.TAG_SOUL_BONUS;

public class Real_souleat_realform extends NoLevelsModifier
        implements SlotStackModifierHook {

    public static final ResourceLocation REAL_REVEALED = getResource("real_revealed");

    private static final float REQUIRED_SOUL = 10_000_000f;

    /* ---------------- SlotStack：只负责“开关” ---------------- */

    @Override
    public boolean overrideOtherStackedOnMe(
            IToolStackView tool,
            ModifierEntry entry,
            ItemStack held,
            Slot slot,
            Player player,
            SlotAccess access
    ) {
//        if (player.level().isClientSide) return false;

        ModDataNBT data = tool.getPersistentData();

        // 读取噬魂
        float soulBonus = data.getFloat(TAG_SOUL_BONUS);

        if (soulBonus < REQUIRED_SOUL) {
            player.displayClientMessage(Component.literal("§8牠还没感到满足。"), true);
            return true;
        }
        boolean a = !data.getBoolean(REAL_REVEALED);
        data.putBoolean(REAL_REVEALED, a);
        if (a){
            player.displayClientMessage(Component.literal("§6牠开始显露真实的形态……"), true);
        } else {
            player.displayClientMessage(Component.literal("§7真实形态被重新封印。"), true);
        }

        return true; // ⭐ 和 Death_eye 一样，必须 true
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
    }
}