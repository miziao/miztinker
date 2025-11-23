package com.mizi.miztinker.modifier.modifiers.base;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.apache.commons.lang3.ObjectUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;

public class ForceHurtUtil {
    public static sun.misc.Unsafe U = null;
    static {
        try {
            Constructor<sun.misc.Unsafe> c = sun.misc.Unsafe.class.getDeclaredConstructor();
            c.setAccessible(true);
            U = c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * 不可回血数据
     */
    public static class UnhealableEntityData extends SynchedEntityData {
        public float maxHealth = Float.MAX_VALUE;

        public UnhealableEntityData(SynchedEntityData old) {
            super(old.entity);
            this.entity = old.entity;
            this.isDirty = old.isDirty;
            this.itemsById = old.itemsById;
            this.lock = old.lock;
        }

        @Override
        public <T extends Object> T get(EntityDataAccessor<T> p_135371_) {
            if (p_135371_ == LivingEntity.DATA_HEALTH_ID && entity instanceof LivingEntity livingEntity) {
                float now = super.get(LivingEntity.DATA_HEALTH_ID);
                maxHealth = (maxHealth > now ? now : maxHealth);
                set(LivingEntity.DATA_HEALTH_ID, Math.max(0, Math.min(maxHealth, now)), true);
            }
            return super.get(p_135371_);
        }

        @Override
        public <T extends Object> void set(EntityDataAccessor<T> p_135382_, T p_135383_) {
            if (p_135382_ == LivingEntity.DATA_HEALTH_ID && entity instanceof LivingEntity livingEntity) {
                float now = super.get(LivingEntity.DATA_HEALTH_ID);
                maxHealth = (maxHealth > now ? now : maxHealth);
                set(LivingEntity.DATA_HEALTH_ID, Math.max(0, Math.min(maxHealth, now)), true);
            }
            super.set(p_135382_, p_135383_);
        }

        @Override
        public <T> void set(EntityDataAccessor<T> p_276368_, T p_276363_, boolean p_276370_) {
            DataItem<T> dataitem = (DataItem<T>) this.getItem(p_276368_);
            if (p_276370_ || ObjectUtils.notEqual(p_276363_, dataitem.getValue())) {
                dataitem.setValue(p_276363_);
                this.entity.onSyncedDataUpdated(p_276368_);
                dataitem.setDirty(true);
                this.isDirty = true;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> DataItem<T> getItem(EntityDataAccessor<T> p_135380_) {
            this.lock.readLock().lock();
            DataItem<T> dataitem;
            try {
                dataitem = (DataItem<T>) this.itemsById.get(p_135380_.getId());
            } catch (Throwable var9) {
                CrashReport crashreport = CrashReport.forThrowable(var9, "Getting synched entity data");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Synched entity data");
                crashreportcategory.setDetail("Data ID", p_135380_);
                throw new ReportedException(crashreport);
            } finally {
                this.lock.readLock().unlock();
            }
            return dataitem;
        }
    }

    /*
     * 强制伤害，如果有错误问我
     */
    public static void forceHurt(LivingEntity target, DamageSource source, float damage) {
        if (target.isSleeping() && !target.level().isClientSide) {
            target.stopSleeping();
        }
        target.noActionTime = 0;
        boolean flag = false;
        float f1 = 0.0F;
        Entity entity1;
        LivingEntity livingentity1;
        if (source.getEntity() != null && source.getEntity() instanceof LivingEntity livingEntity)
            target.lastHurtByMob = livingEntity;
        target.walkAnimation.setSpeed(0.0F);
        target.lastHurt = damage;
        target.invulnerableTime = 0;
        target.getCombatTracker().recordDamage(source, damage);
        {
            SynchedEntityData data = target.getEntityData();
            SynchedEntityData.DataItem<Float> item = data.getItem(LivingEntity.DATA_HEALTH_ID);
            item.setValue(target.getHealth() - damage);
            target.onSyncedDataUpdated(LivingEntity.DATA_HEALTH_ID);
            item.setDirty(true);
            data.isDirty = true;
        }
        if (target.getHealth() <= 0.0F) {
            try {
                target.dropAllDeathLoot(source);
                target.captureDrops(new ArrayList<>());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        target.gameEvent(GameEvent.ENTITY_DAMAGE);
        target.hurtDuration = 10;
        target.hurtTime = target.hurtDuration;
        entity1 = source.getEntity();
        if (entity1 != null) {
            if (entity1 instanceof LivingEntity) {
                livingentity1 = (LivingEntity) entity1;
                if (!source.is(DamageTypeTags.NO_ANGER)) {
                    target.setLastHurtByMob(livingentity1);
                }
            }
            if (entity1 instanceof Player) {
                Player player1 = (Player) entity1;
                target.lastHurtByPlayerTime = 100;
                target.lastHurtByPlayer = player1;
            } else if (entity1 instanceof TamableAnimal) {
                TamableAnimal tamableEntity = (TamableAnimal) entity1;
                if (tamableEntity.isTame()) {
                    target.lastHurtByPlayerTime = 100;
                    LivingEntity livingentity2 = tamableEntity.getOwner();
                    if (livingentity2 instanceof Player) {
                        Player player = (Player) livingentity2;
                        target.lastHurtByPlayer = player;
                    } else {
                        target.lastHurtByPlayer = null;
                    }
                }
            }
        }
        if (flag) {
            target.level().broadcastEntityEvent(target, (byte) 29);
        } else {
            target.level().broadcastDamageEvent(target, source);
        }
        target.hurtMarked = true;
        if (Minecraft.getInstance().isSameThread()) {
            target.playSound(SoundEvents.GENERIC_HURT, 1.0F, target.getVoicePitch());
        }
        boolean flag2 = !flag || damage > 0.0F;
        if (flag2) {
            target.lastDamageSource = source;
            target.lastDamageStamp = target.level().getGameTime();
        }
        if (target instanceof ServerPlayer) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer) target, source, damage, damage, flag);
            if (f1 > 0.0F && f1 < 3.4028235E37F) {
                ((ServerPlayer) target).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD), Math.round(f1 * 10.0F));
            }
        }
        if (entity1 instanceof ServerPlayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) entity1, target, source, damage, damage, flag);
        }
    }

    /*
     * 只禁疗
     */
    public static void makeNoHealable(LivingEntity target) {
        float health = target.getHealth();
        if (!(target.getEntityData() instanceof UnhealableEntityData)) {
            try {
                U.ensureClassInitialized(UnhealableEntityData.class);
                U.putIntVolatile(target.getEntityData(), 8, U.getIntVolatile(U.allocateInstance(UnhealableEntityData.class), 8));
                if (target.getEntityData() instanceof UnhealableEntityData) {
                    UnhealableEntityData unhealableEntityData = (UnhealableEntityData) target.getEntityData();
                    unhealableEntityData.maxHealth = health;
                } else {
                    throw new RuntimeException("klass head isn't apply");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * 强制改血并且不可回血
     */
    public static void forceHurtWithNoHealable(LivingEntity target, DamageSource source, float damage) {
        if (!target.level().isClientSide()) {
            float health = target.getHealth();
            if (!(target.getEntityData() instanceof UnhealableEntityData)) {
                try {
                    U.ensureClassInitialized(UnhealableEntityData.class);
                    U.putIntVolatile(target.getEntityData(), 8, U.getIntVolatile(U.allocateInstance(UnhealableEntityData.class), 8));
                    if (target.getEntityData() instanceof UnhealableEntityData) {
                        UnhealableEntityData unhealableEntityData = (UnhealableEntityData) target.getEntityData();
                        unhealableEntityData.maxHealth = health;
                    } else {
                        throw new RuntimeException("klass head isn't apply");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        forceHurt(target, source, damage);
    }

    /*
     * 从禁疗中回复正常
     */
    public static void recoverFromNoHealable(LivingEntity target) {
        if (!target.level().isClientSide()) {
            if (target.getEntityData() instanceof UnhealableEntityData) {
                try {
                    UnhealableEntityData unhealableEntityData = (UnhealableEntityData) target.getEntityData();
                    unhealableEntityData.maxHealth = Float.MAX_VALUE;
                    U.ensureClassInitialized(SynchedEntityData.class);
                    U.putIntVolatile(target.getEntityData(), 8, U.getIntVolatile(U.allocateInstance(SynchedEntityData.class), 8));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
