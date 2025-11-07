package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.sounds.MiztinkerSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class Plumber extends NoLevelsModifier implements InventoryTickModifierHook {

    private static final double MIN_FALL_SPEED = 0.5; // 最小下落速度触发
    private static final double BOUNCE_UP = 1.0;      // 弹起速度
    private static final float DAMAGE = 10000f;       // 对生物造成伤害

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!(holder instanceof Player player)) return;
        if (!isCorrectSlot) return; // 只在正确槽位
        if (player.isFallFlying()) return; // 滑翔中不触发

        // 获取玩家的下落速度
        Vec3 motion = player.getDeltaMovement();
        if (motion.y > -MIN_FALL_SPEED) return; // 下落速度不够，不触发

        // 获取玩家脚下碰撞的实体
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(0.5, 0.1, 0.5),
                e -> e != player && player.getY() > e.getY() + e.getBbHeight() * 0.5);

        for (LivingEntity entity : entities) {
            // 对实体造成伤害
            entity.hurt(player.damageSources().playerAttack((Player) holder), DAMAGE);

            // 让玩家弹起
            player.setDeltaMovement(motion.x, BOUNCE_UP, motion.z);

            // 播放音效
            player.playSound(MiztinkerSounds.MARIO.get());

            break; // 一次只处理一个生物
        }
    }
}