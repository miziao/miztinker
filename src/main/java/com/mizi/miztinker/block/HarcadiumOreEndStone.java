package com.mizi.miztinker.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class HarcadiumOreEndStone extends Block {
    public HarcadiumOreEndStone() {
        super(
                Properties.of()
                        .strength(50.0f, 2000.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
    }
}
