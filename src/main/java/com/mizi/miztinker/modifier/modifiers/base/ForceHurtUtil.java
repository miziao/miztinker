package com.mizi.miztinker.modifier.modifiers.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.momosensei.momotinker.util.PenetratingDamage;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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


@SuppressWarnings("removal")
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

    public static void tryModifyHealth(LivingEntity entity, float newHealth) {
        float health = entity.getHealth();
        float testValue = health - 1;
        for (Class<?> currentClass = entity.getClass(); currentClass != LivingEntity.class.getSuperclass(); currentClass = currentClass.getSuperclass()) {
            for (var field : currentClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(entity);
                        Class<?> fieldType = field.getType();
                        if (fieldType == float.class || fieldType == Float.class) {
                            field.set(entity, testValue);
                        } else if (fieldType == double.class || fieldType == Double.class) {
                            field.set(entity, (double) testValue);
                        } else if (fieldType == int.class || fieldType == Integer.class) {
                            field.set(entity, (int) testValue);
                        } else if (fieldType == long.class || fieldType == Long.class) {
                            field.set(entity, (long) testValue);
                        } else if (fieldType == short.class || fieldType == Short.class) {
                            field.set(entity, (short) testValue);
                        } else if (fieldType == String.class) {
                            field.set(entity, Float.toString(testValue));
                        }
                        if (entity.getHealth() == testValue) {
                            if (fieldType == float.class || fieldType == Float.class) {
                                field.set(entity, newHealth);
                            } else if (fieldType == double.class || fieldType == Double.class) {
                                field.set(entity, (double) newHealth);
                            } else if (fieldType == int.class || fieldType == Integer.class) {
                                field.set(entity, (int) newHealth);
                            } else if (fieldType == long.class || fieldType == Long.class) {
                                field.set(entity, (long) newHealth);
                            } else if (fieldType == short.class || fieldType == Short.class) {
                                field.set(entity, (short) newHealth);
                            } else if (fieldType == String.class) {
                                field.set(entity, Float.toString(newHealth));
                            }
                        } else {
                            field.set(entity, value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        for (var key : entity.getEntityData().itemsById.keySet()) {
            @SuppressWarnings("unchecked")
            SynchedEntityData.DataItem<Object> item = (SynchedEntityData.DataItem<Object>) entity.getEntityData().itemsById.get(key.intValue());
            var value = item.getValue();
            Class<?> fieldType = value != null ? value.getClass() : null;
            if (fieldType == float.class || fieldType == Float.class) {
                item.setValue(testValue);
            } else if (fieldType == double.class || fieldType == Double.class) {
                item.setValue((double) testValue);
            } else if (fieldType == int.class || fieldType == Integer.class) {
                item.setValue((int) testValue);
            } else if (fieldType == long.class || fieldType == Long.class) {
                item.setValue((long) testValue);
            } else if (fieldType == short.class || fieldType == Short.class) {
                item.setValue((short) testValue);
            } else if (fieldType == String.class) {
                item.setValue(Float.toString(testValue));
            }
            if (entity.getHealth() == testValue) {
                if (fieldType == float.class || fieldType == Float.class) {
                    item.setValue(newHealth);
                } else if (fieldType == double.class || fieldType == Double.class) {
                    item.setValue((double) newHealth);
                } else if (fieldType == int.class || fieldType == Integer.class) {
                    item.setValue((int) newHealth);
                } else if (fieldType == long.class || fieldType == Long.class) {
                    item.setValue((long) newHealth);
                } else if (fieldType == short.class || fieldType == Short.class) {
                    item.setValue((short) newHealth);
                } else if (fieldType == String.class) {
                    item.setValue(Float.toString(newHealth));
                }
            } else {
                item.setValue(value);
            }
        }
    }

    public static void executeall(LevelAccessor world, double x, double y, double z, LivingEntity damager) {
        if (damager instanceof Player player) {
            if (!damager.getCommandSenderWorld().isClientSide) {
                Vec3 vec3 = new Vec3(x, y, z);
                List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, (new AABB(vec3, vec3)).inflate(200F), (e) -> true).stream().sorted(Comparator.comparingDouble((_entcnd) -> _entcnd.distanceToSqr(vec3))).toList();
                for (LivingEntity entity : list) {
                    PenetratingDamage.reflectionPenetratingDamage(entity,player, entity.getMaxHealth());
                    entity.onRemovedFromWorld();
                    entity.remove(Entity.RemovalReason.KILLED);
                    entity.setPos(Double.NaN, Double.NaN, Double.NaN);
                }
            }
        }
    }

    public static class IncreasingOnlyHealthData extends SynchedEntityData {
        public float minHealth = -Float.MAX_VALUE;

        public IncreasingOnlyHealthData(SynchedEntityData old) {
            super(old.entity);
            this.entity = old.entity;
            this.isDirty = old.isDirty;
            this.itemsById = old.itemsById;
            this.lock = old.lock;
        }

        @Override
        public <T> T get(EntityDataAccessor<T> accessor) {
            T value = super.get(accessor);
            if (accessor == LivingEntity.DATA_HEALTH_ID && value instanceof Float now) {
                if (now < minHealth) {
                    return (T) Float.valueOf(minHealth);
                } else {
                     minHealth = now;
                }
            }
            return value;
        }

        @Override
        public <T> void set(EntityDataAccessor<T> accessor, T value) {
            if (accessor == LivingEntity.DATA_HEALTH_ID && value instanceof Float now) {
                if (now < minHealth) {
                    value = (T) Float.valueOf(minHealth);
                } else {
                    minHealth = now;
                }
            }
            super.set(accessor, value);
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
    }

    public static void makeHealthIncreasingOnly(LivingEntity target) {
        if (!target.level().isClientSide()) {
            float currentHealth = target.getHealth();
            if (!(target.getEntityData() instanceof IncreasingOnlyHealthData)) {
                try {
                    U.ensureClassInitialized(IncreasingOnlyHealthData.class);
                    U.putIntVolatile(target.getEntityData(), 8,
                            U.getIntVolatile(U.allocateInstance(IncreasingOnlyHealthData.class), 8));

                    if (target.getEntityData() instanceof IncreasingOnlyHealthData data) {
                        data.minHealth = currentHealth;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("无法应用只能增加血量的数据结构", e);
                }
            }
        }
    }

    public static void recoverToNormalHealth(LivingEntity target) {
        if (!target.level().isClientSide()) {
            if (target.getEntityData() instanceof IncreasingOnlyHealthData) {
                try {
                    U.ensureClassInitialized(SynchedEntityData.class);
                    U.putIntVolatile(target.getEntityData(), 8,
                            U.getIntVolatile(U.allocateInstance(SynchedEntityData.class), 8));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class CapDamageHealthData extends SynchedEntityData {

        public CapDamageHealthData(SynchedEntityData old) {
            super(old.entity);
            this.entity = old.entity;
            this.isDirty = old.isDirty;
            this.itemsById = old.itemsById;
            this.lock = old.lock;
        }

        @Override
        public <T> T get(EntityDataAccessor<T> accessor) {
            T value = super.get(accessor);

            if (accessor == LivingEntity.DATA_HEALTH_ID
                    && value instanceof Float now
                    && this.entity instanceof LivingEntity living) {

                float fixed = protectHealthValue(living, now);

                if (Float.compare(fixed, now) != 0) {
                    rawSet(accessor, (T) Float.valueOf(fixed), true);
                    return (T) Float.valueOf(fixed);
                }
            }

            return value;
        }

        @Override
        public <T> void set(EntityDataAccessor<T> accessor, T value) {
            if (accessor == LivingEntity.DATA_HEALTH_ID
                    && value instanceof Float nextHealth
                    && this.entity instanceof LivingEntity living) {

                float fixed = protectHealthValue(living, nextHealth);
                rawSet(accessor, (T) Float.valueOf(fixed), false);
                return;
            }

            rawSet(accessor, value, false);
        }

        @Override
        public <T> void set(EntityDataAccessor<T> accessor, T value, boolean force) {
            if (accessor == LivingEntity.DATA_HEALTH_ID
                    && value instanceof Float nextHealth
                    && this.entity instanceof LivingEntity living) {

                float fixed = protectHealthValue(living, nextHealth);
                rawSet(accessor, (T) Float.valueOf(fixed), force);
                return;
            }

            rawSet(accessor, value, force);
        }

        private <T> void rawSet(EntityDataAccessor<T> accessor, T value, boolean force) {
            DataItem<T> dataitem = (DataItem<T>) this.getItem(accessor);

            if (force || ObjectUtils.notEqual(value, dataitem.getValue())) {
                dataitem.setValue(value);
                this.entity.onSyncedDataUpdated(accessor);
                dataitem.setDirty(true);
                this.isDirty = true;
            }
        }
    }

    private enum DamageProtectMode {
        CAP,
        IMMUNE
    }

    private static class DamageProtectState {
        float value;
        DamageProtectMode mode;
        float protectedHealth;

        DamageProtectState(float value, DamageProtectMode mode, float protectedHealth) {
            this.value = value;
            this.mode = mode;
            this.protectedHealth = protectedHealth;
        }
    }

    private static final java.util.Map<java.util.UUID, DamageProtectState> DAMAGE_PROTECT_MAP =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 直接读取 DATA_HEALTH_ID 的原始值。
     *
     * 重点：
     * 这里不能用 entity.getHealth()，
     * 否则会重新进入 CapDamageHealthData.get()，造成 StackOverflow。
     */
    private static float rawGetHealthNoProtect(LivingEntity entity) {
        if (entity == null) {
            return 1.0f;
        }

        try {
            SynchedEntityData data = entity.getEntityData();
            SynchedEntityData.DataItem<Float> item = data.getItem(LivingEntity.DATA_HEALTH_ID);
            Float value = item.getValue();

            if (value == null || value.isNaN()) {
                return 1.0f;
            }

            return value;
        } catch (Throwable ignored) {
            return 1.0f;
        }
    }

    private static float protectHealthValue(LivingEntity living, float requestedHealth) {
        if (living == null || living.level().isClientSide()) {
            return requestedHealth;
        }

        DamageProtectState state = DAMAGE_PROTECT_MAP.get(living.getUUID());

        if (state == null) {
            return requestedHealth;
        }

        float rawHealth = rawGetHealthNoProtect(living);

        if (Float.isNaN(state.protectedHealth)) {
            state.protectedHealth = Math.max(rawHealth, requestedHealth);
        }

        /*
         * 这里不能写 living.getHealth()
         * 否则会递归：
         * getHealth -> CapDamageHealthData.get -> protectHealthValue -> getHealth
         */
        float currentHealth = Math.max(rawHealth, state.protectedHealth);

        // 回血、加血允许
        if (requestedHealth >= currentHealth) {
            state.protectedHealth = requestedHealth;
            return requestedHealth;
        }

        float loss = currentHealth - requestedHealth;

        if (state.mode == DamageProtectMode.CAP) {
            /*
             * MiziAo：
             * 单次生命值下降最多 value。
             */
            if (loss > state.value) {
                float cappedHealth = currentHealth - state.value;
                cappedHealth = Math.max(cappedHealth, 1.0f);

                state.protectedHealth = cappedHealth;
                return cappedHealth;
            }

            state.protectedHealth = Math.max(requestedHealth, 1.0f);
            return requestedHealth;
        }

        if (state.mode == DamageProtectMode.IMMUNE) {
            /*
             * 完美套 / AP：
             * 生命值下降量 >= value 时完全免疫。
             */
            if (loss >= state.value) {
                return currentHealth;
            }

            state.protectedHealth = Math.max(requestedHealth, 1.0f);
            return requestedHealth;
        }

        return requestedHealth;
    }

    public static void applyGenericDamageCap(LivingEntity target, float cap) {
        applyGenericDamageProtection(target, cap, DamageProtectMode.CAP);
    }

    public static void applyGenericDamageImmune(LivingEntity target, float threshold) {
        applyGenericDamageProtection(target, threshold, DamageProtectMode.IMMUNE);
    }

    private static void applyGenericDamageProtection(LivingEntity target, float value, DamageProtectMode mode) {
        if (target == null || target.level().isClientSide()) {
            return;
        }

        /*
         * 注意：
         * 这里也不要用 target.getHealth()
         * 因为 target 可能已经是 CapDamageHealthData。
         */
        float currentHealth = Math.max(rawGetHealthNoProtect(target), 1.0f);

        DamageProtectState oldState = DAMAGE_PROTECT_MAP.get(target.getUUID());

        if (oldState == null) {
            DAMAGE_PROTECT_MAP.put(
                    target.getUUID(),
                    new DamageProtectState(value, mode, currentHealth)
            );
        } else {
            oldState.value = value;
            oldState.mode = mode;
            oldState.protectedHealth = Math.max(oldState.protectedHealth, currentHealth);
        }

        if (!(target.getEntityData() instanceof CapDamageHealthData)) {
            try {
                U.ensureClassInitialized(CapDamageHealthData.class);
                U.putIntVolatile(
                        target.getEntityData(),
                        8,
                        U.getIntVolatile(U.allocateInstance(CapDamageHealthData.class), 8)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void uncapDamage(LivingEntity target) {
        if (target == null || target.level().isClientSide()) {
            return;
        }

        DAMAGE_PROTECT_MAP.remove(target.getUUID());

        if (target.getEntityData() instanceof CapDamageHealthData) {
            try {
                U.ensureClassInitialized(SynchedEntityData.class);
                U.putIntVolatile(
                        target.getEntityData(),
                        8,
                        U.getIntVolatile(U.allocateInstance(SynchedEntityData.class), 8)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean shouldCancelProtectedDeath(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return false;
        }

        return DAMAGE_PROTECT_MAP.containsKey(entity.getUUID());
    }

    public static float getProtectedSafeHealth(LivingEntity entity) {
        if (entity == null) {
            return 1.0f;
        }

        DamageProtectState state = DAMAGE_PROTECT_MAP.get(entity.getUUID());

        if (state == null) {
            return Math.max(rawGetHealthNoProtect(entity), 1.0f);
        }

        return Math.max(state.protectedHealth, 1.0f);
    }


}
