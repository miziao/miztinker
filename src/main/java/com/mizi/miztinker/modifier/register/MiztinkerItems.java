package com.mizi.miztinker.modifier.register;


import com.mizi.miztinker.item.DynamaxBandItem;
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
            () -> new Item(new Item.Properties()));

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

    public static final RegistryObject<Item> DYNAMAX_BAND = ITEMS.register("dynamax_band",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.dynamax_band"));
                }
            });



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
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_emerald"));
                }
            });

    public static final RegistryObject<Item> PRIMAL_REVERSION_LAVIUM = ITEMS.register("primal_reversion_lavium",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_lavium"));
                }
            });

    public static final RegistryObject<Item> PRIMAL_REVERSION_QIVIUM = ITEMS.register("primal_reversion_qivium",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_qivium"));
                }
            });

    public static final RegistryObject<Item> PRIMAL_REVERSION_RUBY = ITEMS.register("primal_reversion_ruby",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.primal_reversion_ruby"));
                }
            });

    public static final RegistryObject<Item> DAIGO_ITEM = ITEMS.register("daigo_item",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.daigo_item"));
                }
            });

    public static final RegistryObject<Item> MEGA_MANYULLYN_BOOSTER_ENERGY = ITEMS.register("mega_manyullyn_booster_energy",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)) {
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
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.ravenous_ingot"));
                }
            });

    public static final RegistryObject<Item> TERRESTRIAL_ARTIFACT_ITEM= ITEMS.register("terrestrial_artifact_item",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.terrestrial_artifact_item"));
                }
            });

    public static final RegistryObject<Item> ITEM_7_1= ITEMS.register("7_1",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.7_1"));
                }
            });

    public static final RegistryObject<Item> ITEM_7_2= ITEMS.register("7_2",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.7_2"));
                }
            });

    public static final RegistryObject<Item> ITEM_7_3= ITEMS.register("7_3",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.7_3"));
                }
            });

    public static final RegistryObject<Item> RADIATION_ARGENT_ENERGY= ITEMS.register("radiation_argent_energy",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.radiation_argent_energy"));
                }
            });

    public static final RegistryObject<Item> MOZHUA_CAP= ITEMS.register("mozhua_cap",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)) {
            });

    public static final RegistryObject<Item> DX_INGOT= ITEMS.register("dx_ingot",
            () -> new DynamaxBandItem(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.dx_ingot"));
                }
            });



    public static final RegistryObject<Item> NETHER_REACTOR = ITEMS.register(
            "nether_reactor",
            () -> new BlockItem(
                    MiztinkerBlocks.NETHER_REACTOR.get(),
                    new Item.Properties()
            )
    );

}