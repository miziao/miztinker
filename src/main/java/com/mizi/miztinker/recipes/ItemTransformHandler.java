package com.mizi.miztinker.recipes;


import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 掉落物自动转化系统
 * - 铁锭：夜晚 + Y>300，持续 2 分钟 -> 星金锭
 * - 书：雷暴天，持续 2 分钟 -> 暴风所生之物
 * - 无需雷击触发
 * - 玩家附近粒子提示
 */
@Mod.EventBusSubscriber(modid = "miztinker")
public class ItemTransformHandler {

    private static final Map<Integer, TimerData> timers = new ConcurrentHashMap<>();

    static class TimerData {
        final WeakReference<ItemEntity> ref;
        final long startTick;
        final ResourceLocation target;

        TimerData(ItemEntity entity, long startTick, ResourceLocation target) {
            this.ref = new WeakReference<>(entity);
            this.startTick = startTick;
            this.target = target;
        }
    }

    /** 标记生成的掉落物 */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item) {
            ItemStack stack = item.getItem();
            if (stack.getItem() == Items.IRON_INGOT || stack.getItem() == Items.BOOK) {
                item.getPersistentData().putBoolean("miztinker:transformable", true);
            }
        }
    }

    /** 玩家附近扫描掉落物，触发计时 */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (!(event.player.level() instanceof ServerLevel level)) return;

        double range = 32.0; // 检测范围
        AABB area = event.player.getBoundingBox().inflate(range);

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area,
                e -> e.getPersistentData().getBoolean("miztinker:transformable"))) {

            ResourceLocation target;

            // 条件判断
            if (item.getItem().getItem() == Items.IRON_INGOT && !level.isDay() && item.getY() > 300) {
                target = new ResourceLocation("miztinker:starmetal_ingot");
            } else if (item.getItem().getItem() == Items.BOOK && (level.isThundering() || level.isRaining())) {
                target = new ResourceLocation("miztinker:born_of_the_storm");
            } else {
                target = null;
            }

            int id = item.getId();
            long gameTime = level.getGameTime();

            if (target != null) {
                timers.computeIfAbsent(id, k -> new TimerData(item, gameTime, target));
            } else {
                timers.remove(id);
            }
        }

        // 更新 timers
        Iterator<Map.Entry<Integer, TimerData>> it = timers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, TimerData> entry = it.next();
            TimerData td = entry.getValue();
            ItemEntity entity = td.ref.get();

            if (entity == null || entity.isRemoved() || !(entity.level() instanceof ServerLevel lvl)) {
                it.remove();
                continue;
            }

            long elapsed = lvl.getGameTime() - td.startTick;

            // 每秒粒子提示
            if (elapsed % 20 == 0) {
                double x = entity.getX();
                double y = entity.getY() + 0.15;
                double z = entity.getZ();
                lvl.sendParticles(ParticleTypes.ENCHANT, x, y, z, 6, 0.2, 0.2, 0.2, 0.01);
            }

            // 转化
            if (elapsed >= 2400L) {
                ItemStack newStack = new ItemStack(
                        Objects.requireNonNull(lvl.registryAccess().registryOrThrow(Registries.ITEM).get(td.target)),
                        entity.getItem().getCount()
                );
                ItemEntity newEntity = new ItemEntity(lvl, entity.getX(), entity.getY(), entity.getZ(), newStack);
                lvl.addFreshEntity(newEntity);
                entity.discard();
                it.remove();
            }
        }
    }
}