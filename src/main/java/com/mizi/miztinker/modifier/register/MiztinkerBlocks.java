package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.block.Nether_Reactor;
import com.mizi.miztinker.miztinker;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MiztinkerBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, miztinker.MODID);

    public static final RegistryObject<Block> NETHER_REACTOR = BLOCKS.register(
            "nether_reactor",
            Nether_Reactor::new
    );

}