package com.mizi.miztinker.item.tool.until;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.armor.ModifiableArmorItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MiZiTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "miztinker");

    private static void acceptTool(Consumer<ItemStack> output, Supplier<? extends IModifiable> tool) {
        output.accept(new ItemStack(tool.get()));
    }

    private static void acceptArmor(Consumer<ItemStack> output, EnumObject<ArmorItem.Type, ? extends ModifiableArmorItem> armor) {
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            ModifiableArmorItem armorItem = armor.get(type);
            ToolBuildHandler.addVariants(output, armorItem, "");
        }
    }

    private static void addToolItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output tab) {
        Consumer<ItemStack> output = tab::accept;
        acceptArmor(output, MiztinkerTools.soulizationdArmor);
    }

    public static final RegistryObject<CreativeModeTab> TOOL_TAB = CREATIVE_MODE_TABS.register("soulizationdarmor", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.miztinker.soulizationdarmor"))
            .icon(() -> new ItemStack(MiztinkerTools.soulizationdArmor.get(ArmorItem.Type.CHESTPLATE)))
            .displayItems(MiZiTab::addToolItems)
            .build());
}


