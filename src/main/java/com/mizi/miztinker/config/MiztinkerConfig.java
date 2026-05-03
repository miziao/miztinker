package com.mizi.miztinker.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MiztinkerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_DEATH_NOTE_TRANSFORM;

    static {
        BUILDER.push("Recipes");

        ENABLE_DEATH_NOTE_TRANSFORM = BUILDER
                .comment("Whether to allow obtaining the Death Note via item dropping (Book and Quilt falls from Y:320 to Y:-40).",
                        "是否允许通过掉落物转化获得死亡笔记 (书与笔从 Y:320 掉落至 Y:-40)。")
                .translation("config.miztinker.enable_death_note_transform")
                .define("enableDeathNoteTransform", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}