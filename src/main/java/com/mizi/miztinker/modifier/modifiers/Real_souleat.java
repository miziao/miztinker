package com.mizi.miztinker.modifier.modifiers;


import com.mizi.miztinker.modifier.modifiers.base.RealFormBaseModifier;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;

import static com.mizi.miztinker.miztinker.getResource;
import static com.mizi.miztinker.modifier.modifiers.SoulEat.TAG_SOUL_BONUS;

public class Real_souleat extends RealFormBaseModifier implements SlotStackModifierHook {

    /** 是否已解锁真实形态 */
    private static final ResourceLocation REAL_REVEALED =
            new ResourceLocation("miztinker", "real_revealed");

    /** 进化所需噬魂值 */
    private static final float REQUIRED_SOUL = 10_000_000f;

    public Real_souleat(String materialId, MaterialVariantId reMaterialId, String text) {
        super(materialId, reMaterialId, text);
    }

    /**
     * ✅ 真实形态是否显现
     * 现在只由“是否手动触发过”决定
     */
    @Override
    protected boolean shouldRevealRealForm(ToolStack tool, @Nullable LivingEntity holder) {
        ModDataNBT data = tool.getPersistentData();
        return data.getBoolean(REAL_REVEALED);
    }

    /**
     * ✅ 满足条件后，右键工具一次触发进化
     */
    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot,
                                            Player player, SlotAccess access) {

        // 必须空手
        if (!held.isEmpty()) return false;

        // ❗客户端不要吃掉事件
        if (player.level().isClientSide) return false;

        ModDataNBT data = tool.getPersistentData();

        if (data.getBoolean(REAL_REVEALED)) {
            player.displayClientMessage(
                    Component.literal("§7该工具已觉醒真实形态。"),
                    true
            );
            return true;
        }

        String baseKey = MiztinkerModifiers.SOUL_EAT.getId().toString();
        float soulBonus = data.getFloat(
                getResource(baseKey + "." + TAG_SOUL_BONUS)
        );

        if (soulBonus < REQUIRED_SOUL) {
            player.displayClientMessage(
                    Component.literal("§8噬魂尚未饱和……"),
                    true
            );
            return true;
        }

        // ✅ 解锁真实形态
        data.putBoolean(REAL_REVEALED, true);

        player.displayClientMessage(
                Component.literal("§6牠已经吃饱了！"),
                true
        );

        return true;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
    }
}