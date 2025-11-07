package com.mizi.miztinker.item.tool.until;


import com.mizi.miztinker.item.tool.lollipop;
import com.mizi.miztinker.item.tool.tinker_loli_pickaxe;
import com.mizi.miztinker.item.tool.old_sword;
import com.mizi.miztinker.MiztinkerTab;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.deferred.SynchronizedDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.registration.ItemDeferredRegisterExtension;
import slimeknights.tconstruct.library.materials.RandomMaterial;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.helper.ModifierLootingHandler;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.utils.BlockSideHitListener;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.mizi.miztinker.miztinker.MODID;

public class MiztinkerTools extends MiztinkerTab {
    public MiztinkerTools() {
        SlotType.init();
        BlockSideHitListener.init();
        ModifierLootingHandler.init();
        RandomMaterial.init();
    }
    public static void initRegisters() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        TINKER_ITEMS.register(bus);
        CREATIVE_TABS.register(bus);
    }
    public static final ItemDeferredRegisterExtension TINKER_ITEMS = new ItemDeferredRegisterExtension(MODID);
    public static final SynchronizedDeferredRegister<CreativeModeTab> CREATIVE_TABS = SynchronizedDeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> tabTools = CREATIVE_TABS.register(
            "tools", () -> CreativeModeTab.builder()
                    .title(Component.translatable("Miztinker.tool.tab"))
                    .icon(() -> MiztinkerTools.lollipop.get().getRenderTool())
                    .displayItems(MiztinkerTools::addTabItems)
                    .withTabsBefore(MiztinkerTab.TAB.getId())
                    .withSearchBar()
                    .build());

    private static void acceptTool(Consumer<ItemStack> output, Supplier<? extends IModifiable> tool) {
        ToolBuildHandler.addVariants(output, tool.get(), "");
    }

    public static final ItemObject<ModifiableItem> lollipop = TINKER_ITEMS.register("lollipop",()->new lollipop(new Item.Properties().stacksTo(1)));
    public static final ItemObject<ModifiableItem> tinker_loli_pickaxe = TINKER_ITEMS.register("tinker_loli_pickaxe",()->new tinker_loli_pickaxe(new Item.Properties().stacksTo(1)));
    public static final ItemObject<ModifiableItem> old_sword = TINKER_ITEMS.register("old_sword",()->new old_sword(new Item.Properties().stacksTo(1)));

    private static void addTabItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output tab) {
        Consumer<ItemStack> output = tab::accept;
        acceptTool(output, MiztinkerTools.lollipop);
        acceptTool(output, MiztinkerTools.tinker_loli_pickaxe);
        acceptTool(output, MiztinkerTools.old_sword);
    }
}