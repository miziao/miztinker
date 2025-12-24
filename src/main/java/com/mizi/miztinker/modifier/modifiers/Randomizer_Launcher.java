package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Randomizer_Launcher extends NoLevelsModifier implements GeneralInteractionModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (!player.level().isClientSide && source == InteractionSource.RIGHT_CLICK) {

            EntityType<?> randomizerType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("projecte", "mob_randomizer"));

            if (randomizerType != null) {
                Entity entity = randomizerType.create(player.level());

                if (entity != null) {
                    Vec3 eyePos = player.getEyePosition();
                    Vec3 lookVec = player.getLookAngle();

                    entity.moveTo(eyePos.x + lookVec.x * 0.5, eyePos.y + lookVec.y * 0.5, eyePos.z + lookVec.z * 0.5, player.getYRot(), player.getXRot());

                    if (entity instanceof ThrowableProjectile projectile) {
                        projectile.setOwner(player);
                        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                    } else {
                        entity.setDeltaMovement(lookVec.scale(1.5));
                    }

                    player.level().addFreshEntity(entity);


                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sounds.SoundEvents.EGG_THROW, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 0.4F);

                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getPriority() {
        return 50;
    }
}
