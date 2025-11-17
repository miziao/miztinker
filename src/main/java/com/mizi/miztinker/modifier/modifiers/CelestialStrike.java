package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.mizi.miztinker.modifier.register.MiztinkerEffect.HorologiumNoAI;

public class CelestialStrike extends Modifier implements MeleeHitModifierHook {

    private static final int MAX_CHAIN_DEPTH = 8;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, slimeknights.tconstruct.library.modifiers.ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        Level world = context.getLevel();
        if (world.isClientSide) return;

        LivingEntity attacker = context.getAttacker();
        LivingEntity target = context.getLivingTarget();
        if (target == null) return;

        // 只要命中就触发，不管是否造成伤害或目标是否死亡
        target.addEffect(new MobEffectInstance(HorologiumNoAI.get(), 20, 0, false, true, true));

        int level = modifier.getLevel();
        double range = 3.0 * level; // 半径随等级提升
        int maxTargetsPerLevel = 1 + 4 * (level - 1); // 每级可多连4个目标

        List<LivingEntity> hitList = new ArrayList<>();
        hitList.add(target);

        chainLightning(tool, modifier, context, target, damageDealt, range, hitList, 1, maxTargetsPerLevel);
    }

    private void chainLightning(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context,
                                LivingEntity current, float damage, double range,
                                List<LivingEntity> hitList, int depth, int maxTargetsPerLevel) {
        if (depth > MAX_CHAIN_DEPTH) return;

        Level world = context.getLevel();
        LivingEntity attacker = context.getAttacker();

        AABB box = new AABB(
                current.getX() - range, current.getY() - range, current.getZ() - range,
                current.getX() + range, current.getY() + range, current.getZ() + range
        );

        // ✅ 不要求 isAlive，只排除消失的、重复的、自己、带时停的
        List<LivingEntity> nearby = world.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != current
                        && !hitList.contains(e)
                        && !e.hasEffect(HorologiumNoAI.get())
                        && e.isAddedToWorld()
                        && e != attacker
        );

        nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr(current)));

        int targets = 0;
        for (LivingEntity next : nearby) {
            if (targets >= maxTargetsPerLevel) break;

            DamageSource source;
            if (attacker instanceof Player player) {
                source = player.damageSources().playerAttack(player);
            } else {
                source = attacker.damageSources().mobAttack(attacker);
            }

            // 即使攻击无效也执行，不会中断
            try {
                int lastInvulTime = next.invulnerableTime;
                next.invulnerableTime = 0;
                ToolAttackUtil.attackEntitySecondary(source, damage, next, next, true);
                next.invulnerableTime = lastInvulTime;
            } catch (Exception ignored) {}

            // 时停与粒子总会触发
            next.addEffect(new MobEffectInstance(HorologiumNoAI.get(), 20, 0, false, true, true));
            spawnParticleLineWithDelay(world, current, next, depth * 10);

            hitList.add(next);
            targets++;

            // ✅ 即使 current 死了也继续传递
            chainLightning(tool, modifier, context, next, damage, range, hitList, depth + 1, maxTargetsPerLevel);
        }
    }

    private void spawnParticleLineWithDelay(Level world, LivingEntity from, LivingEntity to, int delayTicks) {
        if (!(world instanceof ServerLevel serverWorld)) return;

        serverWorld.getServer().execute(() -> {
            double dx = to.getX() - from.getX();
            double dy = to.getEyeY() - from.getEyeY();
            double dz = to.getZ() - from.getZ();

            int steps = 25; // 更多步数让线更顺滑
            for (int i = 0; i <= steps; i++) {
                double x = from.getX() + dx * i / steps;
                double y = from.getEyeY() + dy * i / steps;
                double z = from.getZ() + dz * i / steps;

                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                        ParticleTypes.ELECTRIC_SPARK, true, x, y, z, 0, 0, 0, 0f, 2
                );

                for (ServerPlayer player : serverWorld.players()) {
                    if (player.distanceToSqr(x, y, z) < 64 * 64) {
                        player.connection.send(packet);
                    }
                }
            }
        });
    }
}