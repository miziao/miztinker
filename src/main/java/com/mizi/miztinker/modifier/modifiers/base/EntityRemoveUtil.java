package com.mizi.miztinker.modifier.modifiers.base;

import com.mizi.miztinker.network.S2CSyncDespawn;
import com.mizi.miztinker.network.S2CSyncSetPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;

public class EntityRemoveUtil {

    /** 直接删除实体（触发 RemovalReason.DISCARDED，但不触发死亡事件） */
    public static void removeEntity(Entity entity) {
        if (entity == null) return;

        entity.remove(Entity.RemovalReason.DISCARDED);

        // 服务端发送同步包给客户端
        if (!entity.level().isClientSide()) {
            S2CSyncDespawn.send(entity.getId());
        }
    }



    /** 强制移除实体（搬到远方 + 杀死生物 + 删除 + 同步客户端） */
    public static void forceRemoveEntity(Entity entity) {
        if (entity == null) return;

        int entityId = entity.getId();
        Level level = entity.level();

        // 断开骑乘关系
        entity.stopRiding();
        entity.getPassengers().forEach(Entity::stopRiding);

        // 先搬到远方
        entity.setPos(1e9, 0, 0);

        // 服务端同步客户端位置
        if (!level.isClientSide()) {
            S2CSyncSetPos.send(entity, 1e9, 0, 0);
        }


        // 删除实体
        removeEntity(entity);
    }

    /** 删除世界中除玩家外的所有实体（可用于 shift+右键释放） */
    public static void removeAll(Level level, Player player) {
        if (level == null) return;

        Predicate<Entity> predicate = new Predicate<Entity>() {
            @Override
            public boolean test(Entity e) {
                return true;
            }
        };

        List<Entity> entities = level.getEntities((Entity) null, (AABB) predicate);

        for (Entity entity : entities) {
            if (entity instanceof Player) continue;

            forceRemoveEntity(entity);
        }
    }
}