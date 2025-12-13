package com.mizi.miztinker.event;


import com.mizi.miztinker.entity.MiztinkerEntityRegister;
import com.mizi.miztinker.gui.hud.MurasamaHUD;
import com.mizi.miztinker.renderer.ScabbardRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import static com.mizi.miztinker.miztinker.MODID;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,modid = MODID)
public class ModEventListener {

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            event.registerAboveAll( "murasama_energy_quantity_hud", MurasamaHUD.Murasama_Energy_Quantity_HUD);
            event.registerAboveAll( "murasama_energy_point_hud", MurasamaHUD.Murasama_Energy_Point_HUD);
        }
    }
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MiztinkerEntityRegister.scabbard_entity.get(), ScabbardRenderer::new);
        event.registerEntityRenderer(MiztinkerEntityRegister.ultimate_slash.get(), NoopRenderer::new);

    }
}
