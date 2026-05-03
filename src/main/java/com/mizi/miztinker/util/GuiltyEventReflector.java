package com.mizi.miztinker.util;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "miztinker")
public class GuiltyEventReflector {

    private static final ModifierId GUILTY_ID = new ModifierId("miztinker", "guilty");
    private static final double RANGE = 7.0D;
    private static final Set<UUID> FORCED_LIST = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity victim && !victim.level().isClientSide) {
            if (victim.isRemoved()) {
                forcePostKillSignal(victim);
            }
        }
    }

    private static void forcePostKillSignal(LivingEntity victim) {
        UUID uuid = victim.getUUID();
        if (!FORCED_LIST.add(uuid)) return;

        AABB area = victim.getBoundingBox().inflate(RANGE);
        List<ServerPlayer> players = victim.level().getEntitiesOfClass(ServerPlayer.class, area);

        for (ServerPlayer player : players) {
            if (player instanceof FakePlayer) continue;

            if (isGuilty(player)) {
                try {
                    DamageSource playerSrc = victim.level().damageSources().playerAttack(player);
                    victim.lastHurtByPlayer = player;
                    victim.lastHurtByPlayerTime = 100;
                    MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(victim, playerSrc));
                    MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingDropsEvent(victim, playerSrc, new java.util.ArrayList<>(), 0, true));
                    CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, victim, playerSrc);
                    player.awardKillScore(victim, victim.deathScore, playerSrc);

                } catch (Throwable ignored) {
                }
                break;
            }
        }
    }

    private static boolean isGuilty(ServerPlayer player) {
        if (hasModifier(player.getMainHandItem())) return true;
        if (hasModifier(player.getOffhandItem())) return true;

        for (net.minecraft.world.item.ItemStack armorStack : player.getArmorSlots()) {
            if (hasModifier(armorStack)) return true;
        }

        return false;
    }

    private static boolean hasModifier(net.minecraft.world.item.ItemStack stack) {
        return !stack.isEmpty() && ModifierUtil.getModifierLevel(stack, GUILTY_ID) > 0;
    }
}