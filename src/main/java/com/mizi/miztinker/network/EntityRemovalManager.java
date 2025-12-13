package com.mizi.miztinker.network;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;

public class EntityRemovalManager {

    /** 完整移除实体（客户端同步 + 服务端删除） */
    public static void removeEntity(Entity entity) {
        if (entity == null || entity.level().isClientSide()) return;

        int entityId = entity.getId();

        // 断开骑乘
        entity.stopRiding();
        entity.getPassengers().forEach(Entity::stopRiding);

        // 先传送到远方
        forceSetPos(entity, 1e9, 0, 0);

        // 同步客户端位置
        S2CSyncSetPos.send(entity, 1e9, 0, 0);

        // 若是活体 → 杀死
        if (entity instanceof LivingEntity living && living.isAlive()) {
            living.die(living.damageSources().genericKill());
        }

        // 服务端删除
        entity.remove(RemovalReason.DISCARDED);

        // 同步客户端删除
        S2CSyncDespawn.send(entityId);
    }

    private static void forceSetPos(Entity entity, double x, double y, double z) {
        entity.setPos(x, y, z);
    }

}
