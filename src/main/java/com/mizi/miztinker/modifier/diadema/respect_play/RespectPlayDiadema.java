package com.mizi.miztinker.modifier.diadema.respect_play;

import com.csdy.tcondiadema.frames.diadema.Diadema;
import com.csdy.tcondiadema.frames.diadema.DiademaType;
import com.csdy.tcondiadema.frames.diadema.movement.DiademaMovement;
import com.csdy.tcondiadema.frames.diadema.range.DiademaRange;
import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import com.mizi.miztinker.util.RespectPlayManager;
import lombok.NonNull;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RespectPlayDiadema extends Diadema {

    public RespectPlayDiadema(DiademaType type, DiademaMovement movement) {
        super(type, movement);
    }

    @Override
    public @NonNull DiademaRange getRange() {
        return new com.csdy.tcondiadema.diadema.api.ranges.SphereDiademaRange(this, 5.0);
    }

    @Override
    protected void perTick() {
        Level level = getLevel();
        if (level.isClientSide) return;

        Vec3 center = getPosition();
        AABB area = new AABB(
                center.x - 2.5, center.y - 1.5, center.z - 2.5,
                center.x + 2.5, center.y + 1.5, center.z + 2.5
        );

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive);

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MiztinkerEffect.RESPECTPLAY.get(), 200, 0, false, false, true));
            RespectPlayManager.applyRespectPlay(target, 200);
        }
    }
}