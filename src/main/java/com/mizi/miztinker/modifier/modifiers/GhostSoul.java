package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

import java.util.Random;

public class GhostSoul extends NoLevelsModifier implements DamageBlockModifierHook {

    private static final Random random = new Random();
    private static final double DODGE_CHANCE = 50; // 50% 固定闪避

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
    }

    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry,
                                   EquipmentContext context, EquipmentSlot slot,
                                   DamageSource damageSource, float amount) {

        if (!(context.getEntity() instanceof Player player)) return false;

        // 任何伤害类型都可触发 50% 闪避
        if (random.nextInt(100) < DODGE_CHANCE) {
            spawnGhostParticles(player);
            return true;
        }

        return false;
    }

    /** 生成幽灵风格粒子 */
    private void spawnGhostParticles(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position();
            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    pos.x, pos.y + 1.0, pos.z,
                    20,
                    0.3, 0.5, 0.3,
                    0.01
            );
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    pos.x, pos.y + 0.5, pos.z,
                    10,
                    0.2, 0.2, 0.2,
                    0.005
            );
        }
    }
}