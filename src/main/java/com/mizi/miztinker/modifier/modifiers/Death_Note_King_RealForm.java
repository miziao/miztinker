package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.modifiers.base.RealFormBaseModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;

import static com.mizi.miztinker.miztinker.getResource;

public class Death_Note_King_RealForm extends RealFormBaseModifier {

    public static final ResourceLocation KING_MODE_REVEALED = getResource("king_mode_revealed");

    public Death_Note_King_RealForm(
            String materialId,
            MaterialVariantId reMaterialId,
            String text
    ) {
        super(materialId, reMaterialId, text);
    }

    @Override
    protected boolean shouldRevealRealForm(ToolStack tool, @Nullable LivingEntity holder) {
        return tool.getPersistentData().getBoolean(KING_MODE_REVEALED);
    }
}