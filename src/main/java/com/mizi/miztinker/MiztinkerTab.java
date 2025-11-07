package com.mizi.miztinker;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.mizi.miztinker.miztinker.MODID;

public class MiztinkerTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(MODID,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("miztinker_tab"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> new ItemStack(MiztinkerItems.DX.get()))
                    .displayItems((enabledFeatures, output) -> {
                        MiztinkerItems.ITEMS.getEntries().forEach(entry -> output.accept(entry.get()));
                    })
                    .build());
}