package com.mizi.miztinker.modifier.register;



import com.mizi.miztinker.item.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.mizi.miztinker.miztinker;

import javax.annotation.Nullable;
import java.util.List;

import static com.mizi.miztinker.modifier.register.MiztinkerBlocks.*;

public class MiztinkerItems {

    // 注册物品的 DeferredRegister
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, miztinker.MODID);

    // 超极巨化马玉灵
    public static final RegistryObject<Item> BIG_MANYULLYN_INGOT = ITEMS.register("big_manyullyn_ingot",
            () -> new Item(new Item.Properties()));

    // 超级极巨化铁
    public static final RegistryObject<Item> MEGA_BIG_IRON_INGOT = ITEMS.register("mega_big_iron_ingot",
            () -> new Item(new Item.Properties()));

    // DX 物品

    public static final RegistryObject<Item> DX = ITEMS.register("dx",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.dx"));
                }
            });
    public static final RegistryObject<Item> IGNISTER = ITEMS.register("ignister",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> TEN_THOUSAND_SOULS_ITEM = ITEMS.register("ten_thousand_souls_item",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ABYSS_SILVER_ITEM = ITEMS.register("abyss_silver_item",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ONI_MIKO_BOW = ITEMS.register("oni_miko_bow",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.oni_miko_bow"));
                    tooltip.add(Component.translatable("tooltip.miztinker.oni_miko_bow_2"));
                }
            });

    public static final RegistryObject<Item> BRON_OF_THE_STORM = ITEMS.register("born_of_the_storm",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.born_of_the_storm"));
                }
            });

    public static final RegistryObject<Item> SALT_INGOT = ITEMS.register("salt_ingot",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });
    public static final RegistryObject<Item> MEGASTONE_IRON = ITEMS.register("megastone_iron",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.megastone_iron"));
                }
            });

    public static final RegistryObject<Item> DYNAMAX_BAND = ITEMS.register(
            "dynamax_band",
            () -> new DynamaxBandItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
    );



    public static final RegistryObject<Item> HOW_THE_STEEL_WAS_TEMPERED = ITEMS.register("how_the_steel_was_tempered",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> STARMETAL_INGOT = ITEMS.register("starmetal_ingot",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.starmetal_ingot"));
                }
            });

    public static final RegistryObject<Item> FUMO_GOLD_INGOT = ITEMS.register("fumo_gold_ingot",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {

                @Override
                public boolean isFoil(ItemStack stack) {
                    return true; // 总是显示附魔光效
                }

                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.fumo_gold_ingot"));
                }
            });

    public static final RegistryObject<Item> LAVIUM_NEW = ITEMS.register("lavium_new",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)) {
            });

    public static final RegistryObject<Item> QIVIUM_NEW = ITEMS.register("qivium_new",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)) {
            });

    public static final RegistryObject<Item> QIVIUM_INGOT_OLD = ITEMS.register("qivium_ingot_old",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
            });

    public static final RegistryObject<Item> LAVIUM_INGOT_OLD = ITEMS.register("lavium_ingot_old",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
            });


    public static final RegistryObject<Item> EMERALD_GEM_OLD = ITEMS.register("emerald_gem_old",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
            });


    public static final RegistryObject<Item> RUBY_GEM_OLD = ITEMS.register("ruby_gem_old",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
            });

    public static final RegistryObject<Item> PRIMAL_REVERSION_EMERALD = ITEMS.register("primal_reversion_emerald",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_emerald"));
                }
            });

    public static final RegistryObject<Item> PRIMAL_REVERSION_LAVIUM = ITEMS.register("primal_reversion_lavium",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_lavium"));
                }
            });

    public static final RegistryObject<Item> PRIMAL_REVERSION_QIVIUM = ITEMS.register("primal_reversion_qivium",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_qivium"));
                }
            });

    public static final RegistryObject<Item> PRIMAL_REVERSION_RUBY = ITEMS.register("primal_reversion_ruby",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_ruby"));
                }
            });

    public static final RegistryObject<Item> PRIMAL_REVERSION_CACTUS = ITEMS.register("primal_reversion_cactus",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_cactus"));
                }
            });

    public static final RegistryObject<Item> DAIGO_ITEM = ITEMS.register("daigo_item",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.daigo_item"));
                }
            });

    public static final RegistryObject<Item> MEGA_MANYULLYN_BOOSTER_ENERGY = ITEMS.register("mega_manyullyn_booster_energy",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.mega_manyullyn_booster_energy"));
                }
            });

    public static final RegistryObject<Item> PINK = ITEMS.register("pink",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.EPIC)
                    .food(new FoodProperties.Builder()
                            .nutrition(5) // 饥饿值
                            .saturationMod(0.1f) // 饱和度
                            .alwaysEat()
                            // 效果 (持续20t * 秒数)
                            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 15, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 15, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 20 * 15, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.HEAL, 20 * 10, 4), 1.0f)
                            .build())
            ) {
                @Override
                public UseAnim getUseAnimation(ItemStack stack) {
                    return UseAnim.DRINK; // 饮料动画
                }

                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.pink"));
                }
            });
    public static final RegistryObject<Item> BLACK = ITEMS.register("black",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.EPIC)
                    .food(new FoodProperties.Builder()
                            .nutrition(5)
                            .saturationMod(0.1f)
                            .alwaysEat()
                            .effect(() -> new MobEffectInstance(MobEffects.HARM, 20 * 2, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 20 * 15, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 15, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DARKNESS, 20 * 15, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 20 * 15, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.WITHER, 20 * 15, 2), 1.0f)
                            .build())
            ) {
                @Override
                public UseAnim getUseAnimation(ItemStack stack) {
                    return UseAnim.DRINK;
                }

                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.black"));
                }
            });

    public static final RegistryObject<Item> RAVENOUS_INGOT= ITEMS.register("ravenous_ingot",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.ravenous_ingot"));
                }
            });

    public static final RegistryObject<Item> TERRESTRIAL_ARTIFACT_ITEM= ITEMS.register("terrestrial_artifact_item",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.terrestrial_artifact_item"));
                }
            });

    public static final RegistryObject<Item> ITEM_7_1= ITEMS.register("7_1",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.7_1"));
                }
            });

    public static final RegistryObject<Item> ITEM_7_2= ITEMS.register("7_2",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.7_2"));
                }
            });

    public static final RegistryObject<Item> ITEM_7_3= ITEMS.register("7_3",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.7_3"));
                }
            });

    public static final RegistryObject<Item> RADIATION_ARGENT_ENERGY= ITEMS.register("radiation_argent_energy",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.radiation_argent_energy"));
                }
            });

    public static final RegistryObject<Item> MOZHUA_CAP = ITEMS.register(

            "mozhua_cap",
            () -> new MozhuaCapItem(
                    new Item.Properties()
                            .stacksTo(64)
                            .rarity(Rarity.RARE)
            ){
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.mozhua_cap"));
                }
            }
    );

    public static final RegistryObject<Item> DX_INGOT= ITEMS.register("dx_ingot",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.dx_ingot"));
                }
            });


    public static final RegistryObject<Item> MAIMAI_FULL= ITEMS.register("maimai_full",
            () -> new Item(new Item.Properties().stacksTo(2).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.maimai_full"));
                }
            });

    public static final RegistryObject<Item> MAIMAI_1 = ITEMS.register("maimai_1",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> MAIMAI_2 = ITEMS.register("maimai_2",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> MAIMAI_3 = ITEMS.register("maimai_3",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> COPPER_COIN = ITEMS.register("copper_coin",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)) {
            });

    public static final RegistryObject<Item> IRON_COIN = ITEMS.register("iron_coin",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> GOLD_COIN = ITEMS.register("gold_coin",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.gold_coin"));
                }
            });

    public static final RegistryObject<Item> DIAMOND_COIN = ITEMS.register("diamond_coin",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
            });

    public static final RegistryObject<Item> NETHERITE_COIN = ITEMS.register("netherite_coin",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.netherite_coin"));
                }
            });

    public static final RegistryObject<Item> VILLAGER_BUSINESS_CARD = ITEMS.register("villager_business_card", Villager_business_card::new);

    public static final RegistryObject<Item> DEATH_NOTE= ITEMS.register("death_note",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.death_note"));
                }
            });

    public static final RegistryObject<Item> PARASITIC_IRON_ITEM = ITEMS.register("parasitic_iron_item",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)) {
            });

    public static final RegistryObject<Item> TOOTH_STEEL_ITEM = ITEMS.register("tooth_steel_item",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)) {
            });

    public static final RegistryObject<Item> MEET_POLYMER_ITEM = ITEMS.register("meet_polymer_item",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> ALPHA_POLYMER_ITEM = ITEMS.register("alpha_polymer_item",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
            });

    public static final RegistryObject<Item> HARCADIUM = ITEMS.register(
            "harcadium",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.RARE)
            )
    );


    public static final RegistryObject<Item> NETHER_REACTOR = ITEMS.register(
            "nether_reactor",
            () -> new BlockItem(
                    MiztinkerBlocks.NETHER_REACTOR.get(),
                    new Item.Properties()
            )
    );

    public static final RegistryObject<Item> HARCADIUM_ORE = ITEMS.register(
            "harcadium_ore",
            () -> new BlockItem(
                    MiztinkerBlocks.HARCADIUM_ORE.get(),
                    new Item.Properties()
            )
    );

    public static final RegistryObject<Item> HARCADIUM_ORE_END_STONE = ITEMS.register(
            "harcadium_ore_end_stone",
            () -> new BlockItem(
                    MiztinkerBlocks.HARCADIUM_ORE_END_STONE.get(),
                    new Item.Properties()
            )
    );

    public static final RegistryObject<Item> OLD_CACTUS_ITEM = ITEMS.register("old_cactus",
            () -> new BlockItem(OLD_CACTUS.get(), new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.RARE)
            )
    );

    public static final RegistryObject<Item> Tinker_lantern = ITEMS.register("tinker_lantern",
            () -> new BlockItem(TINKER_LANTERN.get(), new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.RARE)
            ){
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.tinker_lantern"));
                }
            }
    );

    public static final RegistryObject<Item> smelteryIncreaseProductionBlock = ITEMS.register("smeltery_increase_production",
            () -> new BlockItem(SMELTERY_INCREASE_PRODUCTION.get(), new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.UNCOMMON)
            ){
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.smeltery_increase_production"));
                }
            }
    );

    public static final RegistryObject<Item> smelteryIncreaseProductionBlock_Pro1 = ITEMS.register("smeltery_increase_production_pro1",
            () -> new BlockItem(SMELTERY_INCREASE_PRODUCTION_PRO1.get(), new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.RARE)
            ){
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.smeltery_increase_production_pro1"));
                }
            }
    );

    public static final RegistryObject<Item> smelteryIncreaseProductionBlock_Pro2 = ITEMS.register("smeltery_increase_production_pro2",
            () -> new BlockItem(SMELTERY_INCREASE_PRODUCTION_PRO2.get(), new Item.Properties()
                    .stacksTo(64)
                    .rarity(Rarity.EPIC)
            ){
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.smeltery_increase_production_pro2"));
                }
            }
    );

    public static final RegistryObject<Item> UNO_CARD= ITEMS.register("uno_card",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.uno_card"));
                    tooltip.add(Component.translatable("tooltip.miztinker.uno_card_2"));
                }
            });

    public static final RegistryObject<Item> SOUL_ESSENCE_FRAGMENTS= ITEMS.register("soul_essence_fragments",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.soul_essence_fragments"));
                    tooltip.add(Component.translatable("tooltip.miztinker.soul_essence_fragments_2"));
                }
            });

    public static final RegistryObject<Item> SOUL_ESSENCE= ITEMS.register("soul_essence",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.soul_essence"));
                }
            });

    public static final RegistryObject<Item> SCULK_MIXTURE= ITEMS.register("sculk_mixture",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.sculk_mixture"));
                }
            });

    public static final RegistryObject<Item> TITAN_CATALYST= ITEMS.register("titan_catalyst",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.titan_catalyst"));
                }
                @Override
                public boolean isFoil(ItemStack stack) {
                    return true;
                }
            });

    public static final RegistryObject<Item> EXP_SHARE= ITEMS.register("exp_share",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.exp_share"));
                    tooltip.add(Component.translatable("tooltip.miztinker.exp_share_2"));
                    tooltip.add(Component.translatable("tooltip.miztinker.exp_share_3"));
                }
            });

    public static final RegistryObject<Item> LENS_SOUL_THE_MINER= ITEMS.register("lens_soul_the_miner",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.lens_soul_the_miner"));
                }
            });

    public static final RegistryObject<Item> DRAGONBONE_TIBETAN = ITEMS.register("dragonbone_tibetan",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
            });

    public static final RegistryObject<Item> MIRAI_NIKKI= ITEMS.register("mirai_nikki",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.mirai_nikki"));
                }
            });

    public static final RegistryObject<Item> MAYBE_PRECISION_MECHANISM= ITEMS.register("maybe_precision_mechanism",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.maybe_precision_mechanism"));
                }
            });

    public static final RegistryObject<Item> IRON_TEETH = ITEMS.register("iron_teeth",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> COPPER_TEETH = ITEMS.register("copper_teeth",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> WITCH_FIBER= ITEMS.register("witch_fiber",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.witch_fiber"));
                }
            });

    public static final RegistryObject<Item> EXP_MOSS= ITEMS.register("exp_moss",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.exp_moss"));
                }
            });

    public static final RegistryObject<Item> EXPERIENCE_STEEL_ALPHA = ITEMS.register("experience_steel_alpha",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> STEEL_DRAGON_BONE = ITEMS.register("steel_dragon_bone",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> FAKEFORGINGHAMMER = ITEMS.register("fake_forging_hammer", FakeForgingHammer::new);

    public static final RegistryObject<Item> REINFORCEMENT_OLD = ITEMS.register("reinforcement_old",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> FLESH_VINE = ITEMS.register("flesh_vine",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
            });

    public static final RegistryObject<Item> BOT_BOOK = ITEMS.register("bot_book", BotBookItem::new);

    public static final RegistryObject<Item> MOSS = ITEMS.register("moss",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)) {
            });

    public static final RegistryObject<Item> MENDING_MOSS= ITEMS.register("mending_moss",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.mending_moss"));
                }
            });

    public static final RegistryObject<Item> DYNAMAX_SAPLING_ITEM = ITEMS.register(
            "dynamax_sapling",
            () -> new BlockItem(MiztinkerBlocks.DYNAMAX_SAPLING.get(), new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.dynamax_sapling"));
                }
            }
    );

    public static final RegistryObject<Item> ETERNAL_FUEL_ITEM = ITEMS.register(
            "eternal_fuel_module",
            () -> new BlockItem(ETERNAL_FUEL.get(), new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.eternal_fuel_module"));
                }
            }
    );

    public static final RegistryObject<Item> OSIRIS_CARD = ITEMS.register(
            "osiris_card",
            () -> new BlockItem(MiztinkerBlocks.DYNAMAX_SAPLING.get(), new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.osiris_card"));
                }
            }
    );
    public static final RegistryObject<Item> OBELISK_CARD = ITEMS.register(
            "obelisk_card",
            () -> new BlockItem(MiztinkerBlocks.DYNAMAX_SAPLING.get(), new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.obelisk_card"));
                }
            }
    );

    public static final RegistryObject<Item> RA_CARD = ITEMS.register(
            "ra_card",
            () -> new BlockItem(MiztinkerBlocks.DYNAMAX_SAPLING.get(), new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.ra_card"));
                }
            }
    );






}