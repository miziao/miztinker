package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.RealFormBaseModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;

import static com.mizi.miztinker.miztinker.getResource;
import static com.mizi.miztinker.modifier.modifiers.Real_souleat_realform.REAL_REVEALED;

public class Real_souleat extends RealFormBaseModifier {

    public Real_souleat(
            String materialId,
            MaterialVariantId reMaterialId,
            String text
    ) {
        super(materialId, reMaterialId, text);
    }

    @Override
    protected boolean shouldRevealRealForm(ToolStack tool, @Nullable LivingEntity holder) {
        if (!(holder instanceof ServerPlayer)) {
            return false;
        }
        return tool.getPersistentData().getBoolean(REAL_REVEALED);
    }
}