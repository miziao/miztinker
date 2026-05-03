package com.mizi.miztinker.mixins;

import com.mizi.miztinker.modifier.modifiers.base.MizUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Boat.class)
public abstract class BoatAE86Mixin extends Entity {

    @Shadow private float invFriction;
    @Shadow private float deltaRotation;
    @Shadow private Boat.Status status;
    @Shadow private boolean inputLeft;
    @Shadow private boolean inputRight;
    @Shadow private boolean inputUp;
    @Shadow private boolean inputDown;

    @Shadow @Nullable public abstract LivingEntity getControllingPassenger();

    @Unique
    private int miztinker$nitroCooldown = 0;

    public BoatAE86Mixin(net.minecraft.world.entity.EntityType<?> type, net.minecraft.world.level.Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void handleNitroCooldown(CallbackInfo ci) {
        if (this.miztinker$nitroCooldown > 0) {
            this.miztinker$nitroCooldown--;
        }
    }

    @Inject(method = "floatBoat", at = @At("TAIL"))
    private void adjustPhysics(CallbackInfo ci) {
        if (MizUtil.hasAE86(this.getControllingPassenger())) {
            this.invFriction = 0.99F;
            if (this.status == Boat.Status.ON_LAND) {
                this.deltaRotation *= 1.1F;
            }
        }
    }

    @Inject(method = "controlBoat", at = @At("TAIL"))
    private void boostEngine(CallbackInfo ci) {
        LivingEntity driver = this.getControllingPassenger();
        if (MizUtil.hasAE86(driver)) {
            if (driver.swingTime == 1 && driver.getMainHandItem().isEmpty() && this.miztinker$nitroCooldown <= 0) {
                float yaw = this.getYRot() * ((float)Math.PI / 180F);
                double nitroPower = 5.5D;
                double mX = (double)Mth.sin(-yaw) * nitroPower;
                double mZ = (double)Mth.cos(yaw) * nitroPower;

                this.setDeltaMovement(mX, this.getDeltaMovement().y, mZ);
                this.miztinker$nitroCooldown = 20;

                if (this.level().isClientSide) {
                    miztinker$spawnNitroParticles(yaw);
                } else {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 5.0F, 1.2F);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 5.8F, 1.5F);
                }
            }

            if (driver instanceof Player player && player.jumping) {
                Vec3 motion = this.getDeltaMovement();
                if (motion.y < 0.7D) {
                    double yUp = (this.status == Boat.Status.ON_LAND) ? 0.6D : 0.1D;
                    this.setDeltaMovement(motion.x, motion.y + yUp, motion.z);
                }
            }

            if (this.status == Boat.Status.ON_LAND || this.status == Boat.Status.IN_AIR) {
                float yaw = this.getYRot() * ((float)Math.PI / 180F);
                Vec3 motion = this.getDeltaMovement();
                double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                double boost = miztinker$calculateBoost(horizontalSpeed);

                if (boost > 0 && !this.inputDown) {
                    this.setDeltaMovement(
                            motion.x + (double)Mth.sin(-yaw) * boost,
                            motion.y,
                            motion.z + (double)Mth.cos(yaw) * boost
                    );
                }
            }

            if (this.inputDown) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.8D, 1.0D, 0.8D));
            }
        }
    }

    @Unique
    private void miztinker$spawnNitroParticles(float yaw) {
        double backX = this.getX() + (double)Mth.sin(yaw) * 1.2D;
        double backZ = this.getZ() - (double)Mth.cos(yaw) * 1.2D;

        for (int i = 0; i < 15; i++) {
            double vx = (this.level().random.nextDouble() - 0.5D) * 0.2D;
            double vy = this.level().random.nextDouble() * 0.2D;
            double vz = (this.level().random.nextDouble() - 0.5D) * 0.2D;

            this.level().addParticle(ParticleTypes.FIREWORK, backX, this.getY() + 0.5D, backZ, vx, vy, vz);
            if (i % 3 == 0) {
                this.level().addParticle(ParticleTypes.EXPLOSION, backX, this.getY() + 0.5D, backZ, 0, 0.05D, 0);
            }
        }
    }

    @Unique
    private double miztinker$calculateBoost(double currentSpeed) {
        double boost = 0;
        if (this.inputUp) {
            boost = 0.3D;
        } else if (this.inputLeft || this.inputRight) {
            boost = 0.15D;
        }
        if (this.status == Boat.Status.IN_AIR) {
            boost *= 0.5;
        }
        if (currentSpeed > 1.2D) {
            boost = Math.max(0, boost * (1.8D - currentSpeed));
        }
        return boost;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void doRammingLogic(CallbackInfo ci) {
        LivingEntity driver = this.getControllingPassenger();
        if (MizUtil.hasAE86(driver)) {
            float yaw = this.getYRot() * ((float)Math.PI / 180F);
            double fX = -Mth.sin(yaw);
            double fZ = Mth.cos(yaw);
            AABB rammingArea = this.getBoundingBox().inflate(0.2D).move(fX * 0.5D, 0, fZ * 0.5D);
            List<Entity> list = this.level().getEntities(this, rammingArea, e -> e != driver);
            for (Entity target : list) {
                if (target instanceof LivingEntity living) {
                    living.hurt(this.damageSources().mobAttack(driver), 5.0F);
                } else if (target instanceof Boat otherBoat) {
                    if (!this.level().isClientSide) {
                        otherBoat.spawnAtLocation(otherBoat.getDropItem());
                        otherBoat.discard();
                    }
                }
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void autoStepAssist(CallbackInfo ci) {
        if (MizUtil.hasAE86(this.getControllingPassenger()) && this.horizontalCollision) {
            float yaw = this.getYRot() * ((float)Math.PI / 180F);
            double fX = -Mth.sin(yaw);
            double fZ = Mth.cos(yaw);
            BlockPos frontPos = BlockPos.containing(this.getX() + fX * 1.35D, this.getY() + 0.1D, this.getZ() + fZ * 1.35D);
            if (this.level().getBlockState(frontPos).isSolidRender(this.level(), frontPos) && this.level().getBlockState(frontPos.above()).isAir()) {
                this.setPos(this.getX(), this.getY() + 1.06D, this.getZ());
            }
        }
    }
}