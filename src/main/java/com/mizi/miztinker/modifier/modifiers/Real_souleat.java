package com.mizi.miztinker.modifier.modifiers;


import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.modifiers.base.RealFormBaseModifier;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;

import static com.mizi.miztinker.modifier.modifiers.SoulEat.TAG_SOUL_BONUS;


public class Real_souleat extends RealFormBaseModifier {

    public Real_souleat(String materialId, MaterialVariantId reMaterialId, String text) {
        super(materialId, reMaterialId, text);
    }

    /**
     * 判断是否满足“真实形态”条件
     * —— 当噬魂加成 ≥ 100 时进化
     */
    @Override
    protected boolean shouldRevealRealForm(ToolStack tool, @Nullable LivingEntity holder) {
        if (holder == null) return false;

        ModDataNBT data = tool.getPersistentData();
        String baseKey = MiztinkerModifiers.SOUL_EAT.getId().toString();

        float soulBonus = data.getFloat(ResourceLocation.parse(baseKey + "." + TAG_SOUL_BONUS));

        // ⚡ 打印调试信息，确认值
        //Minecraft.getInstance().player.sendSystemMessage(Component.literal("soulBonus = " + Thread.currentThread().getName()));
        //System.out.println("soulBonus = " + soulBonus);

        return soulBonus >= 10_000_000f;
    }
}

