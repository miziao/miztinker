package com.mizi.miztinker.modifier.diadema.miziao;

import com.mizi.miztinker.modifier.modifiers.base.EntityRemoveUtil;
import com.mizi.miztinker.network.packets.PlaySoundPacket;
import com.csdy.tcondiadema.diadema.api.ranges.SphereDiademaRange;
import com.csdy.tcondiadema.frames.diadema.Diadema;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.diadema.movement.DiademaMovement;
import com.csdy.tcondiadema.frames.diadema.range.DiademaRange;
import com.mizi.miztinker.network.MiztinkerSyncing;
import com.mizi.miztinker.sounds.MiztinkerSounds;
import lombok.NonNull;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurtWithNoHealable;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.forceSetAllCandidateHealth;


public class MusicGameDiadema extends Diadema {

    private static final double RADIUS = 16.0; // 效果范围
    private static final int SOUND_INTERVAL = 200; // 200 ticks = 10秒
    private static final int DAMAGE_DELAY = 35; // 25 ticks = 1.25秒

    private int soundTickCounter = 0;
    private int damageTickCounter = -1; // -1 表示未激活


    private final Entity holder = getCoreEntity();

    private final Map<LivingEntity, Integer> hitCounter = new HashMap<>();

    public MusicGameDiadema(DiademaType type, DiademaMovement movement) {
        super(type, movement);
    }
    private final SphereDiademaRange range = new SphereDiademaRange(this, RADIUS);

    @Override
    public @NonNull DiademaRange getRange() {
        return range;
    }


    @Override
    protected void perTick() {
        // 确保只在服务器端运行
        if (getLevel().isClientSide) {
            return;
        }

        Entity holder = getCoreEntity();
        if (holder == null) {
            return; // 如果核心实体不存在，直接返回
        }

        // 计时器，用于播放音效
        soundTickCounter++;
        if (soundTickCounter >= SOUND_INTERVAL) {
            soundTickCounter = 0;

            // 注意：这里我们传递的是一个包含了所有潜在目标的列表
            playSoundToEntities(affectingEntities);

            // 激活伤害延迟计时器
            damageTickCounter = 0;
        }

        // 伤害延迟计时器
        if (damageTickCounter != -1) {
            damageTickCounter++;
            if (damageTickCounter >= DAMAGE_DELAY) {
                damageTickCounter = -1; // 重置计时器

                // 3. 将同一个列表传递给伤害和效果施加方法
                applyDamageAndSlowness();
            }
        }
    }





    private void applyDamageAndSlowness() {
        Entity holder = getCoreEntity();
        if (holder == null) return;

        for (Entity entity : affectingEntities) {
            if (entity == holder) continue;
            if (!(entity instanceof LivingEntity living)) continue;

            // 跳跃躲避
            if (living.getDeltaMovement().y > 0.1D) continue;

            // 直接算出伤害（让血量归零）
            float hp = living.getHealth();
            float damage = hp; // 伤害 = 当前HP

            // ① 使用不可回血版本的 hurt（受伤动画 + 声音 + lastHurt + 无法回血）
            forceHurtWithNoHealable(
                    living,
                    holder.damageSources().generic(),
                    damage
            );

            // ② 确保血量真的归零（同步到客户端）
            forceSetAllCandidateHealth(living, 0F);

            // ③ 如果不是玩家，直接强制移除（不管是否死亡）
            if (!(living instanceof Player)) {
                EntityRemoveUtil.forceRemoveEntity(living);
            }
        }
    }


    private void playSoundToEntities(Collection<? extends Entity> entitiesToNotify) {
        Entity holder = getCoreEntity();
        if (holder == null || holder.level().isClientSide()) {
            return;
        }

        for (Entity entity : entitiesToNotify) {
            if (entity instanceof ServerPlayer serverPlayer) {
                MiztinkerSyncing.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new PlaySoundPacket(
                                serverPlayer.getEyePosition(),
                                MiztinkerSounds.DISCONNECTED.get().getLocation(),
                                2.0f,
                                1.0f
                        )
                );
            }
        }
    }
}
