package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurtWithNoHealable;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.forceSetAllCandidateHealth;

public class Death_Note extends NoLevelsModifier implements GeneralInteractionModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool,
                                       ModifierEntry modifier,
                                       Player player,
                                       InteractionHand hand,
                                       InteractionSource source) {

        // 客户端直接 PASS
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        // 只在右键触发
        if (source != InteractionSource.RIGHT_CLICK) {
            return InteractionResult.PASS;
        }

        // 只有蹲下时才触发
        if (!player.isCrouching()) {
            return InteractionResult.PASS;
        }

        // 工具损坏时 PASS
        if (tool.isBroken()) {
            return InteractionResult.PASS;
        }

        // 确保是服务端世界
        if (!(player.level() instanceof ServerLevel level)) {
            return InteractionResult.PASS;
        }

        // ✅ 改成从玩家手中获取 ItemStack
        ItemStack stack = player.getItemInHand(hand);
        String name = stack.getHoverName().getString();
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null) {
            return InteractionResult.FAIL;
        }

        var targetType = BuiltInRegistries.ENTITY_TYPE
                .getOptional(id)
                .orElse(null);

        if (targetType == null) {
            return InteractionResult.FAIL;
        }

        // 对目标实体造成效果
        for (LivingEntity living : level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(512)
        )) {
            if (living.getType() != targetType) continue;
            if (!living.isAlive()) continue;

            float hp = living.getHealth();

            forceHurtWithNoHealable(
                    living,
                    level.damageSources().generic(),
                    hp
            );

            forceSetAllCandidateHealth(living, 0F);
        }

        return InteractionResult.SUCCESS;
    }
}