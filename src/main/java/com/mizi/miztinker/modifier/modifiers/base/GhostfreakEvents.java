package com.mizi.miztinker.modifier.modifiers.base;


import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GhostfreakEvents {

    /** 拒绝玩家死亡，同时瞬间回满血 */
    @SubscribeEvent
    public static void ghostfreakImmortal(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!GhostfreakHelper.isGhostActive(player)) return;

        // ⚡ 关键：瞬间回满血
        player.setHealth(player.getMaxHealth());

        // 拒绝死亡
        event.setCanceled(true);
    }

    /** 拒绝所有伤害 */
    @SubscribeEvent
    public static void ghostfreakInvulnerable(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!GhostfreakHelper.isGhostActive(player)) return;

        // ⚡ 拒绝伤害
        event.setCanceled(true);

        // 再次确保血量满
        player.setHealth(player.getMaxHealth());
    }
}