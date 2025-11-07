package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/**
 * 当玩家不站在地面上时，增加挖掘速度
 */
public class Configuration extends NoLevelsModifier implements BreakSpeedModifierHook {
    private static final float BONUS_SPEED = 7.0f;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BREAK_SPEED);
    }

    /**
     * 必须实现 onBreakSpeed，因为你的 Tinkers 版本没有 BreakSpeedContext
     */
    @Override
    public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier, PlayerEvent.BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeed) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity living && !living.onGround()) { // 正确判断空中状态
            event.setNewSpeed(event.getNewSpeed() + BONUS_SPEED);
        }
    }
}