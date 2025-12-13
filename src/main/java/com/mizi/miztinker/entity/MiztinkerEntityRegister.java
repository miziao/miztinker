package com.mizi.miztinker.entity;


import com.mizi.miztinker.entity.ScabbardEntity.ScabbardEntity;
import com.mizi.miztinker.entity.ScabbardEntity.UltimateSlashEntity;
import com.mizi.miztinker.entity.boss.entity.MiziAo;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.deferred.EntityTypeDeferredRegister;

import static com.mizi.miztinker.miztinker.MODID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID)
public class MiztinkerEntityRegister {

    public static final DeferredRegister<EntityType<?>> ENTITY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final EntityTypeDeferredRegister ENTITIES = new EntityTypeDeferredRegister(MODID);



    public static final RegistryObject<EntityType<MiziAo>> MIZI_AO =
            ENTITY.register("mizi_ao",
                    () -> EntityType.Builder.of(
                                    MiziAo::new, MobCategory.MONSTER)
                            .sized(1.2f, 2.55f)
                            .clientTrackingRange(14)
                            .build("miztinker:mizi_ao"));

    public static final RegistryObject<EntityType<ScabbardEntity>> scabbard_entity = registerScabbardEntity("scabbard_entity", MiztinkerEntityRegister.scabbard_entity);
    public static final RegistryObject<EntityType<UltimateSlashEntity>> ultimate_slash = ENTITIES.register("ultimate_slash", () -> EntityType.Builder.<UltimateSlashEntity>of(UltimateSlashEntity::new, MobCategory.MISC).sized(1F, 1F).setTrackingRange(4).setUpdateInterval(1).setCustomClientFactory((spawnEntity, world) -> new UltimateSlashEntity(MiztinkerEntityRegister.ultimate_slash.get(), world)).setShouldReceiveVelocityUpdates(true));
    public static RegistryObject<EntityType<ScabbardEntity>> registerScabbardEntity(String name, RegistryObject<EntityType<ScabbardEntity>> Type){
        return ENTITIES.register(name, () -> EntityType.Builder.<ScabbardEntity>of(ScabbardEntity::new, MobCategory.MISC).sized(1.25F, 1.25F).setTrackingRange(40).setUpdateInterval(1).setCustomClientFactory((spawnEntity, world) -> new ScabbardEntity(Type.get(), world)).setShouldReceiveVelocityUpdates(true));
    }
    public MiztinkerEntityRegister(IEventBus modBus) {
        ENTITY.register(modBus);
    }
}
