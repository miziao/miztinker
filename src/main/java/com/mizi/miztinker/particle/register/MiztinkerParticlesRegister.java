package com.mizi.miztinker.particle.register;


import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.particle.MiziParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
@Mod.EventBusSubscriber(modid = miztinker.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MiztinkerParticlesRegister {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, miztinker.MODID);

    public static final RegistryObject<SimpleParticleType> MIZI_PARTICLE = PARTICLE_TYPES.register("mizi_particle", () -> new SimpleParticleType(false));


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientSetup(RegisterParticleProvidersEvent event) {
        // 注册粒子工厂
        Minecraft.getInstance().particleEngine.register(MiztinkerParticlesRegister.MIZI_PARTICLE.get(), MiziParticle.Provider::new);

    }
}
