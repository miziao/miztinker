package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import static com.mizi.miztinker.modifier.modifiers.SoulEat.TAG_SOUL_BONUS;

public class Real_souleat_realform extends NoLevelsModifier
        implements SlotStackModifierHook, InventoryTickModifierHook {

    private static final ResourceLocation REAL_REVEALED =
            new ResourceLocation("miztinker", "real_revealed");

    private static final float REQUIRED_SOUL = 1f;

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
        // 和 Death_eye 一样：不关心 held
        if (player.level().isClientSide) return true;

        ModDataNBT data = tool.getPersistentData();

        // 读取噬魂
        String baseKey = MiztinkerModifiers.SOUL_EAT.getId().toString();
        float soulBonus = data.getFloat(
                ResourceLocation.parse(baseKey + "." + TAG_SOUL_BONUS)
        );

        if (soulBonus < REQUIRED_SOUL) {
            player.displayClientMessage(
                    Component.literal("§8牠还没感到满足。"),
                    true
            );
            return true;
        }

        boolean nowActive = !data.getBoolean(REAL_REVEALED);
        data.putBoolean(REAL_REVEALED, nowActive);

        if (nowActive) {
            player.displayClientMessage(
                    Component.literal("§6牠开始显露真实的形态……"),
                    true
            );
        } else {
            player.displayClientMessage(
                    Component.literal("§7真实形态被重新封印。"),
                    true
            );
        }

        return true; // ⭐ 和 Death_eye 一样，必须 true
    }

    /* ---------------- InventoryTick：真正生效 ---------------- */

    @Override
    public void onInventoryTick(
            IToolStackView tool,
            ModifierEntry entry,
            Level world,
            LivingEntity holder,
            int itemSlot,
            boolean isSelected,
            boolean isCorrectSlot,
            ItemStack stack
    ) {
        if (world.isClientSide) return;
        if (!(holder instanceof Player player)) return;

        ModDataNBT data = tool.getPersistentData();
        if (!data.getBoolean(REAL_REVEALED)) return;

    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}