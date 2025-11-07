package com.mizi.miztinker.modifier.register;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
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


    public static final RegistryObject<Item> HOW_THE_STEEL_WAS_TEMPERED = ITEMS.register("how_the_steel_was_tempered",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> STARMETAL_INGOT = ITEMS.register("starmetal_ingot",
            () -> new Item(new Item.Properties().stacksTo(8).rarity(Rarity.RARE)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.miztinker.starmetal_ingot"));
                }
            });
}