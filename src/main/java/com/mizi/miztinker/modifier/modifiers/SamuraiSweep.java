package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modifiers.upgrades.melee.SweepingEdgeModifier;

import java.util.List;

public class SamuraiSweep extends NoLevelsModifier implements LeftClickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, MiztinkerHooks.LEFT_CLICK);
    }

    @Override
    public void onLeftClickEmpty(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot equipmentSlot) {
        if (!level.isClientSide) executeSamuraiSweep(tool, player, level);
    }

    @Override
    public void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot equipmentSlot, BlockState state, net.minecraft.core.BlockPos pos) {
        if (!level.isClientSide) executeSamuraiSweep(tool, player, level);
    }

    private void executeSamuraiSweep(IToolStackView tool, Player player, Level level) {
        if (player.getAttackStrengthScale(0.5F) < 0.8F) return;

        double searchRange = player.getAttributeValue(ForgeMod.ENTITY_REACH.get());
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(viewVec.scale(searchRange));

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                level, player, eyePos, reachVec,
                player.getBoundingBox().expandTowards(viewVec.scale(searchRange)).inflate(1.0D),
                e -> !e.isSpectator() && e.isAlive() && e.isPickable()
        );

        Entity primary = (hitResult != null) ? hitResult.getEntity() : null;
        if (primary instanceof LivingEntity target) {
            player.attack(target);
            this.performManualSweep(tool, player, level, target);
        } else {
            this.performManualSweep(tool, player, level, null);
        }

        player.resetAttackStrengthTicker();
    }

    private void performManualSweep(IToolStackView tool, Player player, Level level, Entity excludeTarget) {
        SweepingEdgeModifier sweepingModifier = TinkerModifiers.sweeping.get();

        float sweepLevel = Math.max(1.0f, tool.getModifier(TinkerModifiers.sweeping.getId()).getEffectiveLevel());

        double range = 2.0D + sweepLevel;
        double angleThreshold = 0.4D - (sweepLevel * 0.3D);

        if (level instanceof ServerLevel serverLevel) {
            float yRot = player.getYRot();
            this.spawnSweepParticle(serverLevel, player, yRot);
            int extraParticles = (int) Mth.clamp((0.4 - angleThreshold) * 2, 0, 2);
            for (int i = 1; i <= extraParticles; i++) {
                this.spawnSweepParticle(serverLevel, player, yRot + (i * 180f / extraParticles));
            }
        }

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float sweepingDamage = sweepingModifier.getSweepingDamage(tool, baseDamage);
        List<Entity> list = level.getEntities(player, player.getBoundingBox().inflate(range, 1.0D, range));
        for (Entity entity : list) {
            if (entity instanceof LivingEntity target && entity != player && entity != excludeTarget && entity.isAlive()) {
                Vec3 look = player.getLookAngle();
                Vec3 toTarget = entity.position().subtract(player.position()).normalize();

                if (look.dot(toTarget) > angleThreshold) {
                    boolean hit = ToolAttackUtil.attackEntitySecondary(
                            player.damageSources().playerAttack(player),
                            sweepingDamage, target, target, true);

                    if (hit) {
                        ToolAttackContext targetContext = ToolAttackContext.attacker(player)
                                .target(target, target)
                                .slot(EquipmentSlot.MAINHAND, InteractionHand.MAIN_HAND)
                                .extraAttack()
                                .cooldown(1.0f)
                                .applyAttributes()
                                .build();

                        for (ModifierEntry modEntry : tool.getModifierList()) {
                            modEntry.getHook(ModifierHooks.MELEE_HIT).afterMeleeHit(tool, modEntry, targetContext, sweepingDamage);
                        }
                    }
                }
            }
        }
        player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);
    }

    private void spawnSweepParticle(ServerLevel level, Player player, float yRot) {
        double d0 = -Mth.sin(yRot * ((float)Math.PI / 180F));
        double d1 = Mth.cos(yRot * ((float)Math.PI / 180F));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX() + d0, player.getY(0.5D), player.getZ() + d1,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }
}