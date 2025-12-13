package com.mizi.miztinker.recipes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "miztinker")
public class BlackItemSummonHandler {

    private static final Map<Integer, TimerData> timers = new ConcurrentHashMap<>();
    private static final ResourceLocation BLACK_ITEM = new ResourceLocation("miztinker:black");
    private static final ResourceLocation SUMMON_ENTITY = new ResourceLocation("miztinker:mizi_ao");

    static class TimerData {
        final WeakReference<ItemEntity> ref;
        final long startTick;
        boolean msg30, msg60, msg90;

        TimerData(ItemEntity entity, long tick) {
            this.ref = new WeakReference<>(entity);
            this.startTick = tick;
            this.msg30 = this.msg60 = this.msg90 = false;
        }
    }

    /** 只标记 miztinker:black */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item) {
            ItemStack stack = item.getItem();

            // 正确获取物品
            var blackItem = ForgeRegistries.ITEMS.getValue(BLACK_ITEM);

            if (blackItem != null && stack.getItem() == blackItem) {
                item.getPersistentData().putBoolean("miztinker:black_timer", true);
            }
        }
    }

    /** 玩家 tick 触发扫描和计时 */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!(event.player.level() instanceof ServerLevel level)) return;

        double range = 32.0;
        AABB area = event.player.getBoundingBox().inflate(range);

        // 找到附近的所有黑色掉落物
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area,
                e -> e.getPersistentData().getBoolean("miztinker:black_timer"))) {

            int id = item.getId();

            timers.computeIfAbsent(id, k -> new TimerData(item, level.getGameTime()));
        }

        // 更新所有计时器
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

            double x = entity.getX();
            double y = entity.getY() + 0.15;
            double z = entity.getZ();

            // 🔥 火焰粒子持续效果
            lvl.sendParticles(ParticleTypes.FLAME, x, y, z, 3,
                    0.15, 0.05, 0.15, 0.001);

            // 找附近玩家
            var players = lvl.getEntitiesOfClass(Player.class,
                    new AABB(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32));

            // --- 阶段提示 ---
            if (elapsed >= 600 && !td.msg30) { // 30 秒
                td.msg30 = true;
                for (Player p : players) {
                    p.displayClientMessage(Component.literal("§e某人注意到了这个东西"), false);
                }
            }

            if (elapsed >= 1200 && !td.msg60) { // 60 秒
                td.msg60 = true;
                for (Player p : players) {
                    p.displayClientMessage(Component.literal("§6某人发现这个东西他很讨厌"), false);
                }
            }

            if (elapsed >= 1800 && !td.msg90) { // 90 秒
                td.msg90 = true;
                for (Player p : players) {
                    p.displayClientMessage(Component.literal("§c一种来自某人的厌恶感涌上你的心头，现在停止还来得及"), false);
                }
            }

            // --- 120 秒召唤怪物 ---
            if (elapsed >= 2400) {

                for (Player p : players) {
                    p.displayClientMessage(Component.literal("§4来不及了，他来了！"), false);
                }

                // 召唤怪物
                EntityType<?> type = lvl.registryAccess()
                        .registryOrThrow(Registries.ENTITY_TYPE)
                        .get(SUMMON_ENTITY);

                if (type != null) {
                    var mob = type.create(lvl);
                    if (mob != null) {
                        mob.setPos(x, y, z);
                        lvl.addFreshEntity(mob);
                    }
                }

                entity.discard();
                it.remove();
            }
        }
    }
}

