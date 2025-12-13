package com.mizi.miztinker;

import com.mizi.miztinker.entity.MiztinkerEntityRegister;
import com.mizi.miztinker.entity.boss.entity.MiziAo;
import com.mizi.miztinker.entity.boss.render.MiziAoRenderer;
import com.mizi.miztinker.item.tool.until.MiztinkerTools;
import com.mizi.miztinker.modifier.diadema.ClientDiademaRegister;
import com.mizi.miztinker.modifier.diadema.DiademaRegister;
import com.mizi.miztinker.modifier.register.*;
import com.mizi.miztinker.network.MiztinkerSyncing;
import com.mizi.miztinker.particle.register.MiztinkerParticlesRegister;
import com.mizi.miztinker.sounds.MiztinkerSounds;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
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

    public static ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(MODID);

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    public static final DeferredRegister<EntityType<?>> ENTITY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, miztinker.MODID);

    public miztinker() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        MiztinkerItems.ITEMS.register(modBus);
        MiztinkerModifiers.MODIFIERS.register(modBus);
        MiztinkerSounds.SOUND_EVENTS.register(modBus);
        MiztinkerEffect.EFFECTS.register(modBus);
        MiztinkerEntityRegister.ENTITY.register(modBus);
        MiztinkerBlocks.BLOCKS.register(modBus);
        DiademaRegister.DIADEMA_TYPES.register(modBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientDiademaRegister.CLIENT_DIADEMA_TYPES.register(modBus);
        });
        MiztinkerTab.CREATIVE_MODE_TABS.register(modBus);
        MiztinkerFluidRegister.FLUIDS.register(modBus);
        MiztinkerTools.initRegisters();
        MiztinkerParticlesRegister.PARTICLE_TYPES.register(modBus);

        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        // ⭐⭐⭐ 在这里实例化你的村民交易事件处理类
        new com.mizi.miztinker.recipes.VillagerTradeHandler();
    }
    public static void initOptionalModifiers() {
        MiztinkerOptionalModifiers.voidregisterOptionalModifiers();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void addAttribute(EntityAttributeCreationEvent event) {
        event.put(MiztinkerEntityRegister.MIZI_AO.get(), MiziAo.createAttributes().build());
    }

    @SubscribeEvent
    public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
        //网络包
        MiztinkerSyncing.Init();
        // 以下代码仅在客户端运行
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MusicSlots::init);
    }
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        // 注册你的GeoEntity渲染器
        event.registerEntityRenderer(
                MiztinkerEntityRegister.MIZI_AO.get(),
                MiziAoRenderer::new
        );
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

                    com.mizi.miztinker.MusicSlots.init();
                }
            }
        }






