package com.mizi.miztinker.recipes;

import com.mizi.miztinker.entity.MiztinkerEntityRegister;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "miztinker")
public class WardenTransformHandler {

    private static final ResourceLocation CATALYST_ITEM = ResourceLocation.parse("miztinker:titan_catalyst");
    private static final String TAG_TRANSFORM_TICKS = "miztinker_transform_timer";

    @SubscribeEvent
    public static void onRightClickWarden(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getTarget() instanceof Warden warden)) return;

        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();

        ResourceLocation heldId = held.isEmpty() ? null :
                player.level().registryAccess().registryOrThrow(Registries.ITEM).getKey(held.getItem());

        if (!CATALYST_ITEM.equals(heldId)) return;

        if (warden.getPersistentData().contains(TAG_TRANSFORM_TICKS)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        warden.getPersistentData().putInt(TAG_TRANSFORM_TICKS, 200);

        player.displayClientMessage(Component.translatable("chat.miztinker.titan_warden.warning"), true);
        level.playSound(null, warden.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 2.0f, 0.5f);

        if (!player.isCreative()) {
            held.shrink(1);
        }
    }

    /**
     * 第二步：每 tick 检查 Warden 状态，播放粒子并处理转化
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !(entity instanceof Warden warden)) return;

        CompoundTag data = warden.getPersistentData();
        if (data.contains(TAG_TRANSFORM_TICKS)) {
            int timer = data.getInt(TAG_TRANSFORM_TICKS);

            if (timer > 0) {
                // 倒计时减一
                data.putInt(TAG_TRANSFORM_TICKS, timer - 1);

                // --- 警告粒子效果 ---
                // 每 5 tick 产生一圈强烈的红色粒子和灵魂粒子
                if (timer % 5 == 0) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, warden.getX(), warden.getY() + 1, warden.getZ(), 10, 0.5, 1, 0.5, 0.1);
                    level.sendParticles(ParticleTypes.ANGRY_VILLAGER, warden.getX(), warden.getY() + 2, warden.getZ(), 3, 0.3, 0.5, 0.3, 0);
                }

                // 随着时间推移，声音越来越急促
                if (timer % 20 == 0 && timer < 60) {
                    level.playSound(null, warden.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.5f, 1.5f);
                }
            } else {
                // --- 执行转化 ---
                executeTransform(level, warden);
            }
        }
    }

    private static void executeTransform(ServerLevel level, Warden oldWarden) {
        // 创建泰坦监守者
        // 请确保 MiztinkerEntityRegister.TITAN_WARDEN 存在且正确注册
        Entity titanWarden = MiztinkerEntityRegister.TITAN_WARDEN.get().create(level);

        if (titanWarden != null) {
            titanWarden.moveTo(oldWarden.getX(), oldWarden.getY(), oldWarden.getZ(), oldWarden.getYRot(), oldWarden.getXRot());

            // 粒子大爆发
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, oldWarden.getX(), oldWarden.getY() + 1, oldWarden.getZ(), 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.SONIC_BOOM, oldWarden.getX(), oldWarden.getY() + 1, oldWarden.getZ(), 5, 0.5, 0.5, 0.5, 0.1);

            // 移除旧的监守者并添加新的
            oldWarden.discard();
            level.addFreshEntity(titanWarden);

            // 全局音效：让周围玩家感到震撼
            level.playSound(null, titanWarden.blockPosition(), SoundEvents.WARDEN_DEATH, SoundSource.HOSTILE, 5.0f, 0.2f);
            level.playSound(null, titanWarden.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2.0f, 1.0f);
        }
    }
}