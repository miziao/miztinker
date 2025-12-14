package com.mizi.miztinker.entity.ScabbardEntity;

import com.momosensei.momotinker.util.AttackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.Util;

import java.util.List;

public class UltimateSlashEntity extends Projectile {
    public ToolStack tool;
    public void setToolStack(ToolStack tool){
        this.tool =tool;
    }
    public UltimateSlashEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void defineSynchedData() {
    }
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {}
    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > 14) {
            discard();
            return;
        }
        Entity entity = this.getOwner();
        if (entity == null) {
            return;
        }
        if (entity instanceof Player player && this.level() instanceof ServerLevel serverLevel&&this.tickCount%2==0) {
            List<LivingEntity> ls0 = serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(50));
            for (LivingEntity targets : ls0) {
                if (targets != this.getOwner() && targets != null) {
                    AttackUtil.attackEntity(tool, player, InteractionHand.MAIN_HAND, targets, () -> 1, true, Util.getSlotType(InteractionHand.MAIN_HAND), tool.getStats().get(ToolStats.ATTACK_DAMAGE)+1, 2.25F, false, true, true, true);
                }
            }
        }
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
