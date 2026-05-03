package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.UsingToolModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class AirSolidify extends NoLevelsModifier implements GeneralInteractionModifierHook, UsingToolModifierHook {

    private static final int DURABILITY_COST = 1;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT, ModifierHooks.TOOL_USING);
    }


    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (!tool.isBroken() && tool.getCurrentDurability() >= 20 && source == InteractionSource.RIGHT_CLICK) {
            GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
        return 72000;
    }


    @Override
    public void onUsingTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
        Level level = entity.level();

        if (!level.isClientSide && entity instanceof Player player) {

            if (tool.isBroken() || tool.getCurrentDurability() < DURABILITY_COST) {
                player.stopUsingItem();
                return;
            }

            ToolDamageUtil.damage(tool, DURABILITY_COST, player, player.getItemInHand(entity.getUsedItemHand()));

            float distance = 3.0f;
            Vec3 look = player.getLookAngle();
            BlockPos pos = BlockPos.containing(player.getEyePosition().add(look.scale(distance)));

            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
            }

            if (level.getGameTime() % 5 == 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CANDLE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 0.5f);
            }
        }

        if (level.isClientSide && level.getGameTime() % 2 == 0) {
            Vec3 pPos = entity.getEyePosition().add(entity.getLookAngle().scale(3.0));
            level.addParticle(ParticleTypes.CLOUD, pPos.x, pPos.y, pPos.z, 0, 0.05, 0);
        }
    }

    @Override
    public void afterStopUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration, int timeLeft, ModifierEntry activeModifier) {
        Level level = entity.level();

        if (!level.isClientSide && modifier == activeModifier) {

            net.minecraft.sounds.SoundEvent[] goatHornSounds = {
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(1).value(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(2).value(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3).value(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(4).value(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(5).value(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(6).value(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(7).value()
            };

            net.minecraft.sounds.SoundEvent selected = goatHornSounds[level.random.nextInt(goatHornSounds.length)];

            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    selected, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}