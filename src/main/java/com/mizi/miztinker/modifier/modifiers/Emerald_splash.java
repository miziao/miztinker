package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.*;

public class Emerald_splash extends NoLevelsModifier implements OnAttackedModifierHook {

    private static final int LASER_COUNT = 48;
    private static final float LASER_LENGTH = 20f;
    private static final float STEP = 0.5f;
    private static final float LASER_RADIUS = 1.5f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context,
                           EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (!isDirectDamage) return;

        LivingEntity defender = context.getEntity();
        if (!(defender instanceof Player player)) return;
        Level level = defender.level();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        float damage = Math.max(1.0F, player.getMaxHealth() - player.getHealth());

        AABB search = new AABB(player.blockPosition()).inflate(LASER_LENGTH);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, search,
                e -> e.isAlive() && e != player);
        Set<LivingEntity> hit = new HashSet<>();

        for (int i = 0; i < LASER_COUNT; i++) {
            Vec3 dir = randomUnitVector();
            fireLaser(serverLevel, player, dir, damage, targets, hit);
        }

        ToolDamageUtil.damageAnimated(tool, 1, defender, slotType);
    }

    private void fireLaser(ServerLevel level, Player player, Vec3 direction, float damage,
                           List<LivingEntity> targets, Set<LivingEntity> hit) {
        Vec3 start = player.getEyePosition();
        Vec3 dir = direction.normalize();

        int steps = (int) (LASER_LENGTH / STEP);
        for (int i = 0; i < steps; i++) {
            Vec3 current = start.add(dir.scale(i * STEP));

            // 粒子特效：亮绿色光线 (#32CD32)
            level.sendParticles(
                    new DustParticleOptions(new Vector3f(0.196f, 0.803f, 0.196f), 2.0f),
                    current.x, current.y, current.z,
                    2, 0.05, 0.05, 0.05, 0);

            for (LivingEntity e : targets) {
                if (e.position().distanceToSqr(current) <= LASER_RADIUS * LASER_RADIUS) {
                    if (hit.add(e)) {
                        e.hurt(level.damageSources().magic(), damage);

                        // 播放水花飞溅音效
                        level.playSound(null, e.blockPosition(),
                                SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
                                SoundSource.PLAYERS, 1.0f, 1.2f);
                    }
                }
            }
        }
    }

    private Vec3 randomUnitVector() {
        double theta = RANDOM.nextDouble() * 2 * Math.PI;
        double phi = Math.acos(2 * RANDOM.nextDouble() - 1);
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        return new Vec3(x, y, z);
    }
}