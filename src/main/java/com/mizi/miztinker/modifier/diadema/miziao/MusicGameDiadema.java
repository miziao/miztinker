package com.mizi.miztinker.modifier.diadema.miziao;

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
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;
import java.util.List;

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurt;

public class MusicGameDiadema extends Diadema {

    private static final double RADIUS = 16.0; // 效果范围
    private static final int SOUND_INTERVAL = 200; // 200 ticks = 10秒
    private static final int DAMAGE_DELAY = 25; // 25 ticks = 1.25秒

    private int soundTickCounter = 0;
    private int damageTickCounter = -1; // -1 表示未激活


    private final Entity holder = getCoreEntity();

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

            // --- 判断是否向上跳跃（躲避） ---
            // 如果上升速度大于 0.1 就视为跳跃成功，不受伤害
            if (living.getDeltaMovement().y > 0.1D) {
                continue; // 跳了 → 不受伤
            }

            // --- 不需要 kill()，改用 forceHurt ---
            float hp = living.getHealth();
            float damage = hp * 10.0F;  // 当前生命值 × 10

            living.hurtTime = 0; // reset if needed

            forceHurt(
                    living,
                    holder.damageSources().generic(), // 伤害来源（generic 或你想换成 playerAttack）
                    damage
            );
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
