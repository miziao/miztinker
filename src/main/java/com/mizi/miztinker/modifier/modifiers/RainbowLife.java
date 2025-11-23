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

public class RainbowLife extends NoLevelsModifier implements DamageBlockModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
    }

    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry,
                                   EquipmentContext context, EquipmentSlot slot,
                                   DamageSource damageSource, float amount) {

        if (!(context.getEntity() instanceof Player player))
            return false;

        if (amount <= 0) return true; // 阻断 0 伤害

        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();

        // 把伤害直接变成回血
        float newHealth = Math.min(currentHealth + amount, maxHealth);

        player.setHealth(newHealth);

        spawnHeartParticles(player);

        return true;
    }

    /** 彩虹生命：生成爱心粒子 */
    private void spawnHeartParticles(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position();

            serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    pos.x, pos.y + 1.2, pos.z,
                    8,
                    0.4, 0.5, 0.4,
                    0.05
            );
        }
    }
}
