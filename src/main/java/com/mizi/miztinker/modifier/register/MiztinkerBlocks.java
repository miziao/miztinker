package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.block.*;
import com.mizi.miztinker.miztinker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MiztinkerBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, miztinker.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "miztinker");

    public static final RegistryObject<Block> NETHER_REACTOR = BLOCKS.register(
            "nether_reactor",
            Nether_Reactor::new
    );

    public static final RegistryObject<Block> HARCADIUM_ORE = BLOCKS.register(
            "harcadium_ore",
            HarcadiumOre::new
    );

    public static final RegistryObject<Block> HARCADIUM_ORE_END_STONE = BLOCKS.register(
            "harcadium_ore_end_stone",
            HarcadiumOreEndStone::new
    );

    public static final RegistryObject<Block> SMELTERY_INCREASE_PRODUCTION = BLOCKS.register(
            "smeltery_increase_production",
            () -> new SmelteryIncreaseProductionBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final RegistryObject<BlockEntityType<SmelteryIncreaseProductionBlock.SmelteryIncreaseProductionBlockEntity>> SMELTERY_INCREASE_PRODUCTION_ENTITY =
            BLOCK_ENTITIES.register("smeltery_increase_production_entity",
                    () -> {
                        BlockEntityType<SmelteryIncreaseProductionBlock.SmelteryIncreaseProductionBlockEntity> type =
                                BlockEntityType.Builder.of(
                                        SmelteryIncreaseProductionBlock.SmelteryIncreaseProductionBlockEntity::new,
                                        SMELTERY_INCREASE_PRODUCTION.get()
                                ).build(null);

                        SmelteryIncreaseProductionBlock.BLOCK_ENTITY_TYPE = type;

                        return type;
                    });

    public static final RegistryObject<Block> SMELTERY_INCREASE_PRODUCTION_PRO1 = BLOCKS.register(
            "smeltery_increase_production_pro1",
            () -> new SmelteryIncreaseProductionBlock_Pro1(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final RegistryObject<BlockEntityType<SmelteryIncreaseProductionBlock_Pro1.SmelteryIncreaseProductionBlockEntity>> SMELTERY_INCREASE_PRODUCTION_PRO1_ENTITY =
            BLOCK_ENTITIES.register("smeltery_increase_production_pro1_entity",
                    () -> {
                        BlockEntityType<SmelteryIncreaseProductionBlock_Pro1.SmelteryIncreaseProductionBlockEntity> type =
                                BlockEntityType.Builder.of(
                                        SmelteryIncreaseProductionBlock_Pro1.SmelteryIncreaseProductionBlockEntity::new,
                                        SMELTERY_INCREASE_PRODUCTION.get()
                                ).build(null);

                        SmelteryIncreaseProductionBlock_Pro1.BLOCK_ENTITY_TYPE = type;

                        return type;
                    });

    public static final RegistryObject<Block> SMELTERY_INCREASE_PRODUCTION_PRO2 = BLOCKS.register(
            "smeltery_increase_production_pro2",
            () -> new SmelteryIncreaseProductionBlock_Pro2(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final RegistryObject<BlockEntityType<SmelteryIncreaseProductionBlock_Pro2.SmelteryIncreaseProductionBlockEntity>> SMELTERY_INCREASE_PRODUCTION_PRO2_ENTITY =
            BLOCK_ENTITIES.register("smeltery_increase_production_pro2_entity",
                    () -> {
                        BlockEntityType<SmelteryIncreaseProductionBlock_Pro2.SmelteryIncreaseProductionBlockEntity> type =
                                BlockEntityType.Builder.of(
                                        SmelteryIncreaseProductionBlock_Pro2.SmelteryIncreaseProductionBlockEntity::new,
                                        SMELTERY_INCREASE_PRODUCTION.get()
                                ).build(null);

                        SmelteryIncreaseProductionBlock_Pro2.BLOCK_ENTITY_TYPE = type;

                        return type;
                    });

    public static final RegistryObject<Block> DYNAMAX_SAPLING = BLOCKS.register(
            "dynamax_sapling",
            BlockDynamaxSapling::new
    );

    public static final RegistryObject<Block> TINKER_LANTERN = BLOCKS.register("tinker_lantern",
            () -> new TinkerLanternBlock(BlockBehaviour.Properties.copy(Blocks.LANTERN).noOcclusion()));

    public static final RegistryObject<BlockEntityType<TinkerLanternBlockEntity>> TINKER_LANTERN_BE =
            BLOCK_ENTITIES.register("tinker_lantern_be",
                    () -> BlockEntityType.Builder.of(TinkerLanternBlockEntity::new, TINKER_LANTERN.get()).build(null));

    public static final RegistryObject<Block> OLD_CACTUS = BLOCKS.register("old_cactus",
            () -> new OldCactusBlock(BlockBehaviour.Properties.of()
                    .strength(0.4f)
                    .sound(SoundType.CROP)
                    .noOcclusion()
                    .randomTicks()
            )
    );

    public static final RegistryObject<Block> ETERNAL_FUEL = BLOCKS.register(
            "eternal_fuel_module",
            () -> new EternalFuelBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
            )
    );

    public static final RegistryObject<BlockEntityType<EternalFuelBlock.EternalFuelBlockEntity>> ETERNAL_FUEL_ENTITY =
            BLOCK_ENTITIES.register("eternal_fuel_entity",
                    () -> {
                        BlockEntityType<EternalFuelBlock.EternalFuelBlockEntity> type =
                                BlockEntityType.Builder.of(
                                        EternalFuelBlock.EternalFuelBlockEntity::new,
                                        ETERNAL_FUEL.get()
                                ).build(null);

                        EternalFuelBlock.BLOCK_ENTITY_TYPE = type;
                        return type;
                    });

}