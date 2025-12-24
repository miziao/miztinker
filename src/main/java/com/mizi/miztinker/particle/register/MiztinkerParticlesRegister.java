package com.mizi.miztinker.particle.register;


import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.particle.*;
import com.mizi.miztinker.renderer.murasama.UltimateSlashStrikeParticle;
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
    public static final RegistryObject<SimpleParticleType> smash_down_boom = PARTICLE_TYPES.register("smash_down_boom", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> hug_smash_down_boom = PARTICLE_TYPES.register("hug_smash_down_boom", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ascending_cut = PARTICLE_TYPES.register("ascending_cut", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ultimate_slash_strike = PARTICLE_TYPES.register("ultimate_slash_strike", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> BIG_SONIC_BOOM = PARTICLE_TYPES.register("big_sonic_boom", () -> new SimpleParticleType(false));

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientSetup(RegisterParticleProvidersEvent event) {
        // 注册粒子工厂
        Minecraft.getInstance().particleEngine.register(MiztinkerParticlesRegister.MIZI_PARTICLE.get(), MiziParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(MiztinkerParticlesRegister.smash_down_boom.get(), SmashDownBoomParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(MiztinkerParticlesRegister.hug_smash_down_boom.get(), HugSmashDownBoomParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(MiztinkerParticlesRegister.ascending_cut.get(), AscendingCutParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(MiztinkerParticlesRegister.ultimate_slash_strike.get(), UltimateSlashStrikeParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(MiztinkerParticlesRegister.BIG_SONIC_BOOM.get(), BigSonicBoomParticle.Provider::new);

    }
}
