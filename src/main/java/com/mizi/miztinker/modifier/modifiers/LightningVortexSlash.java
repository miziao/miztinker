package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class LightningVortexSlash extends NoLevelsModifier implements GeneralInteractionModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public @NotNull InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public @NotNull UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
        return 72000;
    }

    @Override
    public void onStoppedUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int chargeTime = getUseDuration(tool, modifier) - timeLeft;
        if (chargeTime < 10) return;

        float chargeMultiplier = Math.min(chargeTime / 20f, 1.5f);
        float power = 1.5f + (chargeMultiplier * 2.0f);

        float yRot = player.getYRot();
        float xRot = player.getXRot();

        float vecX = -Mth.sin(yRot * 0.017453292F) * Mth.cos(xRot * 0.017453292F);
        float vecY = -Mth.sin(xRot * 0.017453292F);
        float vecZ = Mth.cos(yRot * 0.017453292F) * Mth.cos(xRot * 0.017453292F);
        float lens = Mth.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);

        vecX *= power / lens;
        vecY *= power / lens;
        vecZ *= power / lens;

        player.push(vecX, vecY, vecZ);

        player.startAutoSpinAttack(20);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5F, 1.5F);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(2.0D));

        for (LivingEntity target : targets) {
            if (target != player && ToolAttackUtil.isAttackable(player, target)) {
                slimeknights.tconstruct.library.tools.context.ToolAttackContext context =
                        slimeknights.tconstruct.library.tools.context.ToolAttackContext.attacker(player)
                                .target(target)
                                .hand(InteractionHand.MAIN_HAND)
                                .applyAttributes()
                                .cooldown(1.0f)
                                .build();

                ToolAttackUtil.performAttack(tool, context);
            }
        }

        GeneralInteractionModifierHook.finishUsing(tool);
    }
}