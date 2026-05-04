package com.mizi.miztinker.client;

import com.mizi.miztinker.entity.MiztinkerEntityRegister;
import com.mizi.miztinker.entity.boss.render.MiziAoRenderer;
import com.mizi.miztinker.entity.boss.render.TitanWardenRenderer;
import com.mizi.miztinker.item.tool.until.MiztinkerTools;
import com.mizi.miztinker.key.MiztinkerKey;
import com.mizi.miztinker.modifier.register.MiztinkerBlocks;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.tconstruct.library.client.model.TinkerItemProperties;

import static com.mizi.miztinker.item.tool.until.MiztinkerTools.broom;
import static com.mizi.miztinker.miztinker.MODID;
@SuppressWarnings({"deprecation", "removal"})
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(MiztinkerBlocks.TINKER_LANTERN.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(MiztinkerBlocks.DYNAMAX_SAPLING.get(), RenderType.cutout());

            TinkerItemProperties.registerToolProperties(MiztinkerTools.lollipop.get());
            TinkerItemProperties.registerToolProperties(MiztinkerTools.tinker_loli_pickaxe.get());
            TinkerItemProperties.registerToolProperties(MiztinkerTools.old_sword.get());
            TinkerItemProperties.registerToolProperties(broom.get());
            TinkerItemProperties.registerToolProperties(MiztinkerTools.murasama.get());

            TinkerItemProperties.registerBrokenProperty(MiztinkerTools.lollipop.get());
            TinkerItemProperties.registerBrokenProperty(MiztinkerTools.tinker_loli_pickaxe.get());
            TinkerItemProperties.registerBrokenProperty(MiztinkerTools.old_sword.get());
            TinkerItemProperties.registerBrokenProperty(broom.get());
            TinkerItemProperties.registerBrokenProperty(MiztinkerTools.murasama.get());
        });

        com.mizi.miztinker.MusicSlots.init();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MiztinkerEntityRegister.MIZI_AO.get(), MiziAoRenderer::new);
        event.registerEntityRenderer(MiztinkerEntityRegister.TITAN_WARDEN.get(), TitanWardenRenderer::new);

        event.registerBlockEntityRenderer(MiztinkerBlocks.MAIMAI_FULL_BE.get(), context -> new MaimaiFullRenderer());
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinName : event.getSkins()) {
            net.minecraft.client.renderer.entity.player.PlayerRenderer renderer = event.getSkin(skinName);

            if (renderer != null) {
                renderer.addLayer(new com.mizi.miztinker.client.OniMikoBowRender(renderer));
                renderer.addLayer(new com.mizi.miztinker.client.TigaShieldLayer(renderer));
            }
        }
        LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> steve = event.getSkin("default");
        LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> alex = event.getSkin("slim");

        if (steve != null) steve.addLayer(new OniMikoBowRender(steve));
        if (alex != null) alex.addLayer(new OniMikoBowRender(alex));
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(MiztinkerKey.KeyBinding.KEY);
    }
}
