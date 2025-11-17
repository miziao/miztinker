package com.mizi.miztinker;

import com.mizi.miztinker.item.tool.until.MiztinkerTools;
import com.mizi.miztinker.modifier.diadema.DiademaRegister;
import com.mizi.miztinker.modifier.register.*;
import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.sounds.MiztinkerSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.client.model.TinkerItemProperties;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;

import static com.mizi.miztinker.item.tool.until.MiztinkerTools.*;
import static com.mizi.miztinker.miztinker.MODID;
import static com.mojang.text2speech.Narrator.LOGGER;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MODID)
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class miztinker {
    //Minecart mc =Minecart.getInstance();
    public static final String MODID = "miztinker";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(MODID);

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    public miztinker() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册各类注册表到 mod event bus
        CREATIVE_MODE_TABS.register(modBus);

        MiztinkerItems.ITEMS.register(modBus);

        MiztinkerModifiers.MODIFIERS.register(modBus);

        MiztinkerSounds.SOUND_EVENTS.register(modBus);

        MiztinkerEffect.EFFECTS.register(modBus);

        MiztinkerBlocks.BLOCKS.register(modBus);

        DiademaRegister.DIADEMA_TYPES.register(modBus);

        MiztinkerTab.CREATIVE_MODE_TABS.register(modBus);

        MiztinkerFluidRegister.FLUIDS.register(modBus);

        MiztinkerTools.initRegisters();

        MiztinkerNetwork.register();




        // 在 mod event bus 注册 commonSetup
        modBus.addListener(this::commonSetup);

        // 注册到 Forge 事件总线（用于普通运行时事件监听）
        MinecraftForge.EVENT_BUS.register(this);


    }
    public static void initOptionalModifiers() {
        MiztinkerOptionalModifiers.voidregisterOptionalModifiers();
    }


    private void commonSetup ( final FMLCommonSetupEvent event){
            // Some common setup code
        initOptionalModifiers();
        }


            @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
            public static class ClientModEvents {
                @SubscribeEvent
                public static void onClientSetup(FMLClientSetupEvent event) {
                    event.enqueueWork(() -> {
                        TinkerItemProperties.registerToolProperties(lollipop.get());
                        TinkerItemProperties.registerBrokenProperty(lollipop.get());
                        TinkerItemProperties.registerToolProperties(old_sword.get());
                        TinkerItemProperties.registerToolProperties(broom.get());

                        TinkerItemProperties.registerToolProperties(tinker_loli_pickaxe.get());
                        TinkerItemProperties.registerBrokenProperty(tinker_loli_pickaxe.get());
                        TinkerItemProperties.registerBrokenProperty(old_sword.get());
                        TinkerItemProperties.registerBrokenProperty(broom.get());
                    });
                }
            }
        }






