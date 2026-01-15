package com.mizi.miztinker.recipes;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(modid = "miztinker")
public class BotBookTransformHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || event.level.getGameTime() % 20 != 0) return;

        ServerLevel level = (ServerLevel) event.level;

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(-100000, -64, -100000, 100000, 1000, 100000),
                entity -> entity.getItem().is(Items.BOOK));

        for (ItemEntity bookEntity : items) {
            if (bookEntity.isRemoved() || bookEntity.getItem().getCount() != 1) continue;

            AABB area = bookEntity.getBoundingBox().inflate(1.5);
            List<ItemEntity> nearby = level.getEntitiesOfClass(ItemEntity.class, area);

            for (ItemEntity other : nearby) {
                if (other == bookEntity || other.isRemoved()) continue;

                ItemStack otherStack = other.getItem();
                if (otherStack.is(Items.COAL_BLOCK) && otherStack.getCount() == 64) {
                    LOGGER.info("[自然秘典转化] 检测到书与煤炭块重合，开始转化！");
                    performTransform(level, bookEntity, other);
                    return;
                }
            }
        }
    }

    private static void performTransform(ServerLevel level, ItemEntity itemA, ItemEntity itemB) {
        double x = itemA.getX();
        double y = itemA.getY();
        double z = itemA.getZ();

        itemA.discard();
        itemB.discard();

        ItemStack resultStack = new ItemStack(MiztinkerItems.BOT_BOOK.get());
        ItemEntity result = new ItemEntity(level, x, y, z, resultStack);
        result.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(result);

        level.sendParticles(ParticleTypes.FLAME, x, y + 0.3, z, 50, 0.2, 0.2, 0.2, 0.05);
        level.sendParticles(ParticleTypes.LAVA, x, y + 0.3, z, 15, 0.2, 0.2, 0.2, 0.05);
        level.playSound(null, x, y, z, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 0.8f);
    }
}