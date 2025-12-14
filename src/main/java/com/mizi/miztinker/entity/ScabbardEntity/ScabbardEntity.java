package com.mizi.miztinker.entity.ScabbardEntity;


import com.mizi.miztinker.particle.register.MiztinkerParticlesRegister;
import com.momosensei.momotinker.util.AttackUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.Util;

import java.util.List;
import java.util.Random;

import static com.mizi.miztinker.entity.ScabbardEntity.MizitinkerEntitiesMove.*;
import static com.mizi.miztinker.item.tool.murasama.*;


public class ScabbardEntity extends Projectile {
    public ToolStack tool;
    public ToolStack main_hand;
    public float damage = 0;
    public boolean isRotate = false;
    public boolean isReturn = false;
    public int ReturnTime=0;
    public ScabbardEntity(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    private static final EntityDataAccessor<ItemStack> DATA_TOOL = SynchedEntityData.defineId(ScabbardEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> DATA_SPAWN_YAW = SynchedEntityData.defineId(ScabbardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SPAWN_PITCH = SynchedEntityData.defineId(ScabbardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_ROTATING = SynchedEntityData.defineId(ScabbardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_FORM = SynchedEntityData.defineId(ScabbardEntity.class, EntityDataSerializers.INT);

    @Override
    public void defineSynchedData() {
        this.entityData.define(DATA_TOOL, ItemStack.EMPTY);
        this.entityData.define(DATA_SPAWN_YAW, 0f);
        this.entityData.define(DATA_SPAWN_PITCH, 0f);
        this.entityData.define(DATA_ROTATING, false);
        this.entityData.define(DATA_FORM, 0);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {}

    public void setSpawnRotation(float yaw, float pitch) {
        this.entityData.set(DATA_SPAWN_YAW, yaw);
        this.entityData.set(DATA_SPAWN_PITCH, pitch);
    }

    public float getSpawnYaw() {
        return this.entityData.get(DATA_SPAWN_YAW);
    }

    public float getSpawnPitch() {
        return this.entityData.get(DATA_SPAWN_PITCH);
    }
    public void setRotating(boolean rotating) {
        this.entityData.set(DATA_ROTATING, rotating);
    }

    public boolean isRotating() {
        return this.entityData.get(DATA_ROTATING);
    }

    public void setForm(int form) {
        this.entityData.set(DATA_FORM, form);
    }

    public int getForm() {
        return this.entityData.get(DATA_FORM);
    }

    public ToolStack getToolStack() {
        if (this.tool == null) {
            if (!this.level().isClientSide()) {
                if (!(this.getOwner() instanceof Player player)) {
                    this.tool = ToolStack.from(ItemStack.EMPTY);
                } else {
                    ToolStack originalStack = ToolStack.from(player.getMainHandItem());
                    if (isValidToolStack(originalStack)) {
                        this.tool = originalStack.copy();
                    }
                    this.entityData.set(DATA_TOOL, this.tool.createStack());
                }
            } else {
                ItemStack syncedStack = this.entityData.get(DATA_TOOL);
                if (!syncedStack.isEmpty()) {
                    this.tool = ToolStack.from(syncedStack);
                } else {
                    this.tool = ToolStack.from(ItemStack.EMPTY);
                }
            }
        }
        return this.tool;
    }

    public void setToolStack(ToolStack toolStack) {
        this.tool = toolStack;
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_TOOL, toolStack != null ? toolStack.createStack() : ItemStack.EMPTY);
        }
    }

    private boolean isValidToolStack(ToolStack toolStack) {
        if (toolStack == null) return false;
        ItemStack itemStack = toolStack.createStack();
        return !itemStack.isEmpty() && itemStack.getItem() != Items.AIR;
    }

    public ItemStack getItem() {
        if (this.level().isClientSide()) {
            return this.entityData.get(DATA_TOOL);
        }
        ToolStack toolStack = getToolStack();
        return toolStack != null ? toolStack.createStack() : ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        if (shouldDiscard()) return;

        Entity owner = this.getOwner();
        if (owner == null) return;

        spawnTrailParticles();

        if (getForm()==0) {
            if (!isReturn) {
                if (isRotate) {
                    setRotating(true);
                    handleRotateMode(owner);
                    this.ReturnTime++;
                    if (this.ReturnTime >= 20 && !owner.getPersistentData().getBoolean(can_cut_entity.toString())) {
                        setRotating(false);
                        this.isReturn = true;
                    }
                } else {
                    handleProjectileMode(owner);
                }
            }
            if (owner instanceof Player player && this.level() instanceof ServerLevel serverLevel) {
                if (!isReturn) {
                    handlePlayerInteraction(player, serverLevel);
                } else {
                    handleReturnToOwner(player);
                }
                if (this.tickCount >= 80 && !player.getPersistentData().getBoolean(can_cut_entity.toString())) {
                    this.isReturn = true;
                }
            }
        } else if (getForm()==1) {
            if (owner instanceof Player player) {
                makePlayerFall(player);
                maintainRelativePosition(this,player, 0, 0, 0);
                if (player.onGround()){
                    this.onHit(new BlockHitResult(this.position(), Direction.UP,this.blockPosition().below(),false));
                }
            }
        }

        updateRotation();
        super.move(MoverType.SELF, this.getDeltaMovement());
    }
    private void makePlayerFall(Player player) {
        double fallSpeed = -4D;
        Vec3 newMotion = new Vec3(player.getDeltaMovement().x, fallSpeed, player.getDeltaMovement().z);
        player.setDeltaMovement(newMotion);
    }
    private void handleRotateMode(Entity owner) {
        this.setDeltaMovement(Vec3.ZERO);

        if (owner instanceof Player player && this.level() instanceof ServerLevel serverLevel) {
            if (!player.getPersistentData().getBoolean(can_cut_entity.toString())){
                handleRotateAttack(player, serverLevel);
            }
        }
    }

    private void handleRotateAttack(Player player, ServerLevel serverLevel) {
        if (player.tickCount % 4 != 0) return;

        float attackRange = 1.75f;
        ToolStack tool = ToolStack.from(getItem());

        List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(attackRange)
        );

        for (LivingEntity target : nearbyEntities) {
            if (target != null && target != player) {
                AttackUtil.attackEntity(
                        tool, player, InteractionHand.MAIN_HAND, target,
                        () -> 1, true, Util.getSlotType(InteractionHand.MAIN_HAND),
                        tool.getStats().get(ToolStats.ATTACK_DAMAGE) + 1,
                        0.75f, false, true, true, true
                );
            }
        }
    }

    private void handleReturnToOwner(Player player) {
        Vec3 distance = player.position().subtract(this.position());
        this.setDeltaMovement(distance.normalize().scale(3D));
        this.isRotate=false;
        if (distance.length() < 2) {
            markOwnerCanCreateScabbard(player);
            player.getPersistentData().putBoolean(can_cut_entity.toString(), false);
            this.discard();
        }
    }

    private void handleProjectileMode(Entity owner) {
        if (!this.level().isClientSide) {
            LivingEntity target = findNearestTargetWithTransfer(this, owner, 1.5);
            if (target != null) {
                this.setDeltaMovement(Vec3.ZERO);
                moveTowardsTargetWithTransfer(this, target, 3D, false, 0);
            } else {
                executeMovement();
            }
        }
        EntityHitResult entityHit = performEntityCollisionDetection();
        if (entityHit != null && entityHit.getType() != HitResult.Type.MISS) {
            this.onHit(entityHit);
        }
    }

    private EntityHitResult performEntityCollisionDetection() {
        Vec3 startPos = this.position();
        Vec3 endPos = startPos.add(this.getDeltaMovement());
        return ProjectileUtil.getEntityHitResult(
                this.level(), this, startPos, endPos,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(3),
                this::canHitEntity
        );
    }

    private void executeMovement() {
        Vec3 movementVector = calculateMovementVector(getSpawnYaw(), getSpawnPitch());
        this.setDeltaMovement(movementVector.scale(3D));
    }

    private boolean shouldDiscard() {
        int discardTime = 100;
        if (this.tickCount >= discardTime || !isInLoadedChunk(this)) {
            if (this.getOwner() instanceof Player player) {
                markOwnerCanCreateScabbard(player);
                player.getPersistentData().putBoolean(can_cut_entity.toString(), false);
                player.getPersistentData().putBoolean(is_smash_down.toString(),false);
            }
            this.discard();
            return true;
        }
        return false;
    }

    private void spawnTrailParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 pos = this.position().add(0, this.getBbHeight() * 0.5f, 0);
        Vec3 deltaMovement = this.getDeltaMovement();

        for (int i = 0; i < 4; i++) {
            Vec3 trailPos = pos.subtract(deltaMovement.normalize().scale(i * 0.25));
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    trailPos.x, trailPos.y, trailPos.z, 5, 0.1, 0.1, 0.1, 3);
        }
    }

    private void handlePlayerInteraction(Player player, ServerLevel serverLevel) {
        double distanceToPlayer = player.position().subtract(this.position()).length();
        String cutKey = can_cut_entity.toString();

        if (player.getPersistentData().getBoolean(cutKey)) {
            handleCutModeLogic(player, serverLevel, distanceToPlayer);
        } else if (distanceToPlayer >= 40) {
            this.isReturn=true;
            handleReturnToOwner(player);
        }

        if (player.isShiftKeyDown() || player.isDeadOrDying()) {
            markOwnerCanCreateScabbard(player);
            this.discard();
        }
    }

    private void handleCutModeLogic(Player player, ServerLevel serverLevel, double distance) {
        this.setDeltaMovement(Vec3.ZERO);

        if (distance < 3) {
            executeAscendingCut(player, serverLevel);
        } else {
            pullPlayerToEntity(player);
        }
    }

    private void pullPlayerToEntity(Player player) {
        Vec3 distance = this.position().subtract(player.position());
        Vec3 motion = distance.normalize().scale(4D);
        player.setDeltaMovement(motion);
        player.hurtMarked = true;
        player.fallDistance = 0;
    }

    private void executeAscendingCut(Player player, ServerLevel serverLevel) {
        Random random = new Random();
        Vec3 upwardMotion = new Vec3(
                (random.nextDouble() - 0.5) * 0.5,
                random.nextDouble() * 0.4 + 0.4,
                (random.nextDouble() - 0.5) * 0.5
        );
        if (player.level() instanceof ServerLevel serverLevel1) {
            serverLevel1.sendParticles(MiztinkerParticlesRegister.ascending_cut.get(),
                    player.getX(), player.getY()+player.getBbHeight()*0.5F, player.getZ(), 2, 0, 0, 0, 1);
        }
        player.setDeltaMovement(upwardMotion);
        player.hurtMarked = true;

        ToolStack tool = ToolStack.from(getItem());
        float multiplier = 1.5f;
        float attackRange = 3.5f;

        List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(attackRange)
        );

        for (LivingEntity target : nearbyEntities) {
            if (target != this.getOwner() && target != null) {
                AttackUtil.attackEntity(
                        tool, player, InteractionHand.MAIN_HAND, target,
                        () -> 1, true, Util.getSlotType(InteractionHand.MAIN_HAND),
                        tool.getStats().get(ToolStats.ATTACK_DAMAGE) + 1,
                        multiplier, false, true, true, true
                );

                target.setDeltaMovement(upwardMotion.multiply(0.8, 1.1, 0.8));
                target.hurtMarked = true;
                target.fallDistance = 0;
            }
        }
        if (tool.getPersistentData().getFloat(ascending_points)==10){
            if (player.getPersistentData().getFloat(murasam_slash_cooldown.toString())==0
                    &&(tool.getPersistentData().getBoolean(tool_murasama_lock_b)||isTrueNameA(tool)||isTrueNameB(tool))){
                if (player instanceof ServerPlayer player1) {
                    createDimensionSlash(player1);
                }
                Vec3 pos = player.position();
                if (player.level() instanceof ServerLevel level0){
                    level0.sendParticles(MiztinkerParticlesRegister.ultimate_slash_strike.get() ,pos.x,pos.y,pos.z,1,0,0,0,1);
                }
                tool.getPersistentData().putFloat(ascending_points,0);
                player.getPersistentData().putFloat(murasam_slash_cooldown.toString(),20);
            }
        }else if (tool.getPersistentData().getFloat(ascending_points) < 10) {
            tool.getPersistentData().putFloat(
                    ascending_points,
                    tool.getPersistentData().getFloat(ascending_points) + 1
            );
        }
        markOwnerCanCreateScabbard(player);
        String s = can_cut_entity.toString();
        player.getPersistentData().putBoolean(s,false);

        this.discard();
    }

    private void markOwnerCanCreateScabbard(Entity owner) {
        if (owner != null) {
            owner.getPersistentData().putBoolean(cannot_create_scabbard.toString(), false);
        }
    }

    @Override
    public void onHitEntity(@NotNull EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level().isClientSide || !(getOwner() instanceof Player player)) {
            return;
        }
        Entity hitEntity = hitResult.getEntity();
        if (hitEntity == player || !isValidTarget(hitEntity)) {
            return;
        }
        activateCutMode(hitEntity);
    }

    private boolean isValidTarget(Entity entity) {
        return entity instanceof LivingEntity || entity instanceof EnderDragonPart;
    }

    private void activateCutMode(Entity hitEntity) {
        this.isRotate = true;
        this.setDeltaMovement(Vec3.ZERO);

        Vec3 targetPosition = calculateOptimalCutPosition(hitEntity);
        this.setPos(targetPosition);
    }

    private Vec3 calculateOptimalCutPosition(Entity target) {
        return new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.6f, target.getZ());
    }

    @Override
    public void onHitBlock(BlockHitResult p_37258_) {
        super.onHitBlock(p_37258_);
        if (this.level().isClientSide || !(getOwner() instanceof Player player)) {
            return;
        }
        smashDownMode(player);
    }
    private void smashDownMode(Player player) {
        Random random = new Random();
        Vec3 upwardMotion = new Vec3(
                (random.nextDouble() - 0.5) * 0.5,
                random.nextDouble() * 0.6 + 0.75,
                (random.nextDouble() - 0.5) * 0.5
        );
        float vy = (float) Math.abs(Math.pow(player.getDeltaMovement().y, 0.5F));
        player.setDeltaMovement(upwardMotion);
        player.hurtMarked = true;
        player.fallDistance = 0;
        ToolStack tool = ToolStack.from(getItem());
        float multiplier = 1.5f+vy;
        float attackRange = 5f;
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(MiztinkerParticlesRegister.hug_smash_down_boom.get(),
                    player.getX(),player.getY(), player.getZ(), 2, 0.1, 0.1, 0.1, 1);
        }
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(attackRange)
        );

        for (LivingEntity target : nearbyEntities) {
            if (target != this.getOwner() && target != null) {
                AttackUtil.attackEntity(
                        tool, player, InteractionHand.MAIN_HAND, target,
                        () -> 1, true, Util.getSlotType(InteractionHand.MAIN_HAND),
                        tool.getStats().get(ToolStats.ATTACK_DAMAGE) + 1,
                        multiplier, false, true, true, true
                );

                target.setDeltaMovement(upwardMotion.multiply(0.8, 1.1, 0.8));
                target.hurtMarked = true;
                target.fallDistance = 0;
            }
        }
        markOwnerCanCreateScabbard(player);
        player.getPersistentData().putBoolean(is_smash_down.toString(),false);
        this.discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void checkInsideBlocks() {
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
