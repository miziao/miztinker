package com.mizi.miztinker;

import com.mizi.miztinker.client.MaimaiFullRenderer;
import com.mizi.miztinker.config.MiztinkerConfig;
import com.mizi.miztinker.entity.MiztinkerEntityRegister;
import com.mizi.miztinker.entity.boss.entity.MiziAo;
import com.mizi.miztinker.entity.boss.entity.TitanWarden;
import com.mizi.miztinker.entity.boss.render.MiziAoRenderer;
import com.mizi.miztinker.entity.boss.render.TitanWardenRenderer;
import com.mizi.miztinker.item.tool.until.MiZiTab;
import com.mizi.miztinker.item.tool.until.MiztinkerTools;
import com.mizi.miztinker.key.MiztinkerKey;
import com.mizi.miztinker.modifier.diadema.ClientDiademaRegister;
import com.mizi.miztinker.modifier.diadema.DiademaRegister;
import com.mizi.miztinker.modifier.modifiers.base.ServerTickHandler;
import com.mizi.miztinker.modifier.register.*;
import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.particle.register.MiztinkerParticlesRegister;
import com.mizi.miztinker.renderer.murasama.PostPasses;
import com.mizi.miztinker.sounds.MiztinkerSounds;
import com.mizi.miztinker.util.MizTimeStopHandler;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import slimeknights.tconstruct.library.client.model.TinkerItemProperties;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import static com.mizi.miztinker.item.tool.until.MiztinkerTools.*;
import static com.mizi.miztinker.miztinker.MODID;
// The value here should match an entry in the META-INF/mods.toml file

@SuppressWarnings("removal")
@Mod(MODID)
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class miztinker {
    // Minecart mc =Minecart.getInstance();
    public static final String MODID = "miztinker";
    public static ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(MODID);

    public static ResourceLocation location(String string) {
        return ResourceLocation.fromNamespaceAndPath(MODID, string);
    }

    public miztinker() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MiztinkerItems.ITEMS.register(modBus);
        MiztinkerModifiers.MODIFIERS.register(modBus);
        MiztinkerSounds.SOUND_EVENTS.register(modBus);
        MiztinkerEffect.EFFECTS.register(modBus);
        MiztinkerEntityRegister.ENTITY.register(modBus);
        MiztinkerEntityRegister.ENTITIES.register(modBus);
        MiztinkerBlocks.BLOCKS.register(modBus);
        MiztinkerPotions.POTIONS.register(modBus);
        MiztinkerBlocks.BLOCK_ENTITIES.register(modBus);
        MiztinkerLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modBus);
        DiademaRegister.DIADEMA_TYPES.register(modBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientDiademaRegister.CLIENT_DIADEMA_TYPES.register(modBus);
        });
        MiztinkerTab.CREATIVE_MODE_TABS.register(modBus);
        MinecraftForge.EVENT_BUS.register(MizTimeStopHandler.class);
        MiztinkerFluidRegister.FLUIDS.register(modBus);
        MiztinkerTools.initRegisters();
        MiztinkerParticlesRegister.PARTICLE_TYPES.register(modBus);
        MinecraftForge.EVENT_BUS.register(new ServerTickHandler());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MiztinkerConfig.SPEC);
        MiztinkerRegistry.RECIPE_SERIALIZERS.register(modBus);
        MiZiTab.CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(PostPasses::register);
        }
        new com.mizi.miztinker.recipes.VillagerTradeHandler();
    }

    public static void initOptionalModifiers() {
        MiztinkerOptionalModifiers.voidregisterOptionalModifiers();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void addAttribute(EntityAttributeCreationEvent event) {
        event.put(MiztinkerEntityRegister.MIZI_AO.get(), MiziAo.createAttributes().build());
        event.put(MiztinkerEntityRegister.TITAN_WARDEN.get(), TitanWarden.createAttributes().build());
    }

    @SubscribeEvent
    public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
        // 以下代码仅在客户端运行
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MusicSlots::init);
    }

    public static class ClientSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(MiztinkerBlocks.TINKER_LANTERN.get(), RenderType.cutout());
            });
        }
    }

    public static ResourceLocation getResource(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            initOptionalModifiers();
            MiztinkerNetwork.init();
            PotionBrewing.addMix(Potions.MUNDANE, Items.BLAZE_POWDER, MiztinkerPotions.STRENGTH_OLD_POTION.get());
            PotionBrewing.addMix(MiztinkerPotions.STRENGTH_OLD_POTION.get(), Items.REDSTONE, MiztinkerPotions.STRENGTH_OLD_POTION_LONG.get());
            PotionBrewing.addMix(MiztinkerPotions.STRENGTH_OLD_POTION.get(), Items.GLOWSTONE_DUST, MiztinkerPotions.STRENGTH_OLD_POTION_STRONG.get());
        });
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    //@SubscribeEvent
    //public static void onKeyRegister(RegisterKeyMappingsEvent event) {
    //    event.register(MiztinkerKey.KeyBinding.KEY);
    //}
}
