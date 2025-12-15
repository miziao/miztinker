package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.RealFormBaseModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;

public class Real_souleat extends RealFormBaseModifier {

    private static final ResourceLocation REAL_REVEALED =
            new ResourceLocation("miztinker", "real_revealed");

    public Real_souleat(
            String materialId,
            MaterialVariantId reMaterialId,
            String text
    ) {
        super(materialId, reMaterialId, text);
    }

    @Override
    protected boolean shouldRevealRealForm(
            ToolStack tool,
            @Nullable LivingEntity holder
    ) {
        return tool.getPersistentData().getBoolean(REAL_REVEALED);
    }
}