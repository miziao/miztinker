package com.mizi.miztinker.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;



public class Nether_Reactor extends Block {
    public Nether_Reactor() {
        super(
                Properties.of()
                        .strength(5.0f, 1.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.METAL)
        );
    }
}
