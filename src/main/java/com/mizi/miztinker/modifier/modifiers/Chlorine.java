package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.AreaEffectCloud;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ToolDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

public class Chlorine extends NoLevelsModifier implements ToolDamageModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        if (holder != null && amount > 0) {
            Level level = holder.level();
            if (!level.isClientSide) {
                // 每次耐久消耗都生成毒雾云
                level.addFreshEntity(createChlorineCloud(level, holder));
            }
        }
        return amount; // 不修改实际耐久损耗
    }

    /** 创建一个半径为 3 格、持续 40 tick 的中毒云 */
    private static @NotNull AreaEffectCloud createChlorineCloud(Level level, LivingEntity owner) {
        final float radius = 3.0F;
        final int duration = 40; // 40 tick = 2 秒

        AreaEffectCloud cloud = new AreaEffectCloud(level, owner.getX(), owner.getY(), owner.getZ());
        cloud.setOwner(null); // 不关联持有者
        cloud.setFixedColor(0x66FF66); // 浅绿色
        cloud.setParticle(ParticleTypes.ITEM_SLIME); // 可视粒子

        cloud.setRadius(radius);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setRadiusPerTick(-radius / (float) duration);

        cloud.setDuration(duration);
        cloud.setWaitTime(0);

        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 1)); // 中毒 II，持续 40 tick

        return cloud;
    }
}