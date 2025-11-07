package com.mizi.miztinker.client;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.WeakHashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class TigaShieldRenderTracker {

    // 使用 WeakHashMap 保存玩家护盾状态
    private static final Map<Player, Boolean> SHIELD_MAP = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 玩家加入时初始化护盾状态
            SHIELD_MAP.put(player, false);
        }
    }

    // 设置护盾激活状态
    public static void setShieldActive(Player player, boolean active) {
        SHIELD_MAP.put(player, active);
    }

    // 检查护盾是否激活
    public static boolean hasShield(Player player) {
        return SHIELD_MAP.getOrDefault(player, false);
    }

    // 注册事件
    public static void register(IEventBus eventBus) {
        eventBus.addListener(TigaShieldRenderTracker::onPlayerJoin);
    }
}