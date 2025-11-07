package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class RetributionExplosion extends NoLevelsModifier implements OnAttackedModifierHook {


    /** 当玩家/实体受到任何伤害时触发爆炸 */
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    /** 当玩家/实体受到任何伤害时触发爆炸 */
    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context,
                           EquipmentSlot slot, DamageSource source, float amount, boolean isDirectDamage) {
        LivingEntity entity = context.getEntity();
        if (entity == null || entity.level().isClientSide) return;
        if (amount <= 0) return;

        Level level = entity.level();

        // 半径 = 所受伤害（防止炸穿世界）
        float radius = Math.min(amount, 3000.0f);

        // 播放爆炸音效
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        // 创建爆炸（破坏方块）
        level.explode(
                entity,
                entity.getX(), entity.getY(), entity.getZ(),
                radius,
                false, // 不引发火焰
                Level.ExplosionInteraction.BLOCK // ✅ 可以破坏方块
        );

        // 创建爆炸范围
        AABB explosionBox = new AABB(
                entity.getX() - radius, entity.getY() - radius, entity.getZ() - radius,
                entity.getX() + radius, entity.getY() + radius, entity.getZ() + radius
        );

        // 对范围内所有活体造成伤害（不包括自己）
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, explosionBox,
                e -> e != null && e.isAlive() && e != entity);

        for (LivingEntity target : entities) {
            target.hurt(level.damageSources().explosion(entity, entity), amount);
        }

        // 自己不受爆炸伤害，但会被炸飞
        double knockback = radius * 0.1;
        entity.push(
                (level.random.nextDouble() - 0.5) * knockback,
                knockback * 0.5,
                (level.random.nextDouble() - 0.5) * knockback
        );
    }
}