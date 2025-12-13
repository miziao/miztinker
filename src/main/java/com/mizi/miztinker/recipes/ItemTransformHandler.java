package com.mizi.miztinker.recipes;

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

    /** 标记生成的掉落物，并记录初始高度 */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item) {
            ItemStack stack = item.getItem();

            if (stack.getItem() == Items.IRON_INGOT
                    || stack.getItem() == Items.BOOK
                    || stack.getItem() == Items.WRITABLE_BOOK) {

                item.getPersistentData().putBoolean("miztinker:transformable", true);
                item.getPersistentData().putDouble("miztinker:origin_y", item.getY());
            }
        }
    }

    /** 玩家附近扫描掉落物 */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player.level() instanceof ServerLevel level)) return;

        double range = 32.0;
        AABB area = event.player.getBoundingBox().inflate(range);

        for (ItemEntity item : level.getEntitiesOfClass(
                ItemEntity.class,
                area,
                e -> e.getPersistentData().getBoolean("miztinker:transformable")
        )) {

            ResourceLocation target;

            // ===== 原有规则 =====
            if (item.getItem().getItem() == Items.IRON_INGOT
                    && !level.isDay()
                    && item.getY() > 300) {
                target = new ResourceLocation("miztinker:starmetal_ingot");

            } else if (item.getItem().getItem() == Items.BOOK
                    && (level.isThundering() || level.isRaining())) {
                target = new ResourceLocation("miztinker:born_of_the_storm");

            }
            // ===== 新增：书与笔 → 死亡笔记 =====
            else {
                target = null;
                if (item.getItem().getItem() == Items.WRITABLE_BOOK) {
                    double originY = item.getPersistentData().getDouble("miztinker:origin_y");

                    if (originY >= 320 && item.getY() <= -40) {
                        // ✅ 直接立刻转化
                        ItemStack newStack = new ItemStack(
                                Objects.requireNonNull(
                                        level.registryAccess()
                                                .registryOrThrow(Registries.ITEM)
                                                .get(new ResourceLocation("miztinker:death_note"))
                                ),
                                item.getItem().getCount()
                        );

                        ItemEntity newEntity = new ItemEntity(
                                level, item.getX(), item.getY(), item.getZ(), newStack
                        );
                        level.addFreshEntity(newEntity);
                        item.discard();
                    }
                    continue; // 不进入计时器
                }
            }

            // ===== 原有计时转化逻辑 =====
            int id = item.getId();
            long gameTime = level.getGameTime();

            if (target != null) {
                timers.computeIfAbsent(id, k -> new TimerData(item, gameTime, target));
            } else {
                timers.remove(id);
            }
        }

        // ===== 原有 Timer 更新 =====
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

            if (elapsed >= 2400L) {
                ItemStack newStack = new ItemStack(
                        Objects.requireNonNull(
                                lvl.registryAccess()
                                        .registryOrThrow(Registries.ITEM)
                                        .get(td.target)
                        ),
                        entity.getItem().getCount()
                );
                ItemEntity newEntity = new ItemEntity(
                        lvl, entity.getX(), entity.getY(), entity.getZ(), newStack
                );
                lvl.addFreshEntity(newEntity);
                entity.discard();
                it.remove();
            }
        }
    }
}