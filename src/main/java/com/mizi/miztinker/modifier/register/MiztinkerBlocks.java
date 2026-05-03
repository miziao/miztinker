package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.block.*;
import com.mizi.miztinker.miztinker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    public static final RegistryObject<Block> DIAMOND_CONTINENT_PORTAL = BLOCKS.register(
            "diamond_continent_portal",
            DiamondPortalBlock::new
    );

    public static final RegistryObject<BlockEntityType<DynamaxSaplingEntity>> DYNAMAX_SAPLING_ENTITY =
            BLOCK_ENTITIES.register("dynamax_sapling_entity",
                    () -> BlockEntityType.Builder.of(
                            DynamaxSaplingEntity::new,
                            MiztinkerBlocks.DYNAMAX_SAPLING.get()
                    ).build(null)
            );

    public static final RegistryObject<Block> SAPLING_NUGGET = BLOCKS.register("sapling_nugget",
            () -> new SaplingNugget(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));

    public static final RegistryObject<Block> SCORCHED_SEPARATOR = BLOCKS.register("scorched_separator",
            () -> new ScorchedSeparatorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
    );

    public static final RegistryObject<Block> SearedAlloyRetarder = BLOCKS.register("seared_alloy_retarder",
            () -> new SearedAlloyRetarderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
    );

    public static final RegistryObject<Block> REINFORCED_SEARED_BRICK = BLOCKS.register("reinforced_seared_brick",
            () -> new ReinforcedSearedBrickBlock(Block.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)
            ));

    public static final RegistryObject<Block> TINKER_ELECTRICITY_MODULE = BLOCKS.register("tinker_electricity_module",
            () -> new TinkerElectricityModuleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
            ));

    public static final RegistryObject<BlockEntityType<TinkerElectricityModuleBlock.TinkerElectricityModuleBlockEntity>> TINKER_ELECTRICITY_MODULE_ENTITY =
            BLOCK_ENTITIES.register("tinker_electricity_module_entity",
                    () -> {
                        BlockEntityType<TinkerElectricityModuleBlock.TinkerElectricityModuleBlockEntity> type =
                                BlockEntityType.Builder.of(
                                        TinkerElectricityModuleBlock.TinkerElectricityModuleBlockEntity::new,
                                        TINKER_ELECTRICITY_MODULE.get()
                                ).build(null);

                        TinkerElectricityModuleBlock.BLOCK_ENTITY_TYPE = type;
                        return type;
                    });


    public static final RegistryObject<Block> NUTRIENT_SOLUTION_MODULE = BLOCKS.register("nutrient_solution_module",
            () -> new NutrientSolutionModuleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
    );

    public static final RegistryObject<Block> MAIMAI_FULL = BLOCKS.register("maimai_full",
            MaimaiFullBlock::new);

    public static final RegistryObject<BlockEntityType<MaimaiFullBlockEntity>> MAIMAI_FULL_BE =
            BLOCK_ENTITIES.register("maimai_full", () ->
                    BlockEntityType.Builder.of(
                            MaimaiFullBlockEntity::new,
                           MiztinkerBlocks.MAIMAI_FULL.get()
                    ).build(null)
            );
}