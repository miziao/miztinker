package com.mizi.miztinker.entity;


import com.mizi.miztinker.entity.boss.entity.MiziAo;
import com.mizi.miztinker.miztinker;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = miztinker.MODID)
public class MiztinkerEntityRegister {

    public static final DeferredRegister<EntityType<?>> ENTITY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, miztinker.MODID);




    public static final RegistryObject<EntityType<MiziAo>> MIZI_AO =
            ENTITY.register("mizi_ao",
                    () -> EntityType.Builder.of(
                                    MiziAo::new, MobCategory.MONSTER)
                            .sized(1.2f, 2.55f)
                            .clientTrackingRange(14)
                            .build("miztinker:mizi_ao"));

    public MiztinkerEntityRegister(IEventBus modBus) {
        ENTITY.register(modBus);
    }
}
