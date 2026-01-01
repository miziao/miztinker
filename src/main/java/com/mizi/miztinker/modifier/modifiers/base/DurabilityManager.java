package com.mizi.miztinker.modifier.modifiers.base;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

public class DurabilityManager {
    public static final ResourceLocation THEORETICAL_MAX_KEY = new ResourceLocation("miztinker", "theoretical_max_durability");

    public static long getTheoreticalMax(IToolContext context) {
        IModDataView persistentData = context.getPersistentData();

        if (persistentData.contains(THEORETICAL_MAX_KEY, Tag.TAG_LONG)) {
            return persistentData.get(THEORETICAL_MAX_KEY, CompoundTag::getLong);
        }
        return 0L;
    }

    public static void setTheoreticalMax(IToolStackView tool, long value) {
        ModDataNBT persistentData = tool.getPersistentData();
        persistentData.put(THEORETICAL_MAX_KEY, LongTag.valueOf(value));
    }
}