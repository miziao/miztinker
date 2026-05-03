package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class SheepHeadlong extends NoLevelsModifier implements LeftClickModifierHook {

    private static final double DASH_STRENGTH = 0.8;
    private static final double UPWARD_FORCE = 0.25;

    @Override
    public void onLeftClickEmpty(@NotNull IToolStackView tool, @NotNull ModifierEntry entry,
                                 @NotNull Player player, @NotNull Level world,
                                 @NotNull EquipmentSlot slot) {
        performDash(player, world);
    }

    @Override
    public void onLeftClickBlock(@NotNull IToolStackView tool, @NotNull ModifierEntry entry,
                                 @NotNull Player player, @NotNull Level world,
                                 @NotNull EquipmentSlot slot,
                                 @NotNull BlockState state, @NotNull BlockPos pos) {
        performDash(player, world);
    }

    private void performDash(Player player, Level world) {
        Vec3 lookVec = player.getLookAngle();

        Vec3 horizontalLook = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        Vec3 dashVelocity = horizontalLook.scale(DASH_STRENGTH).add(0, UPWARD_FORCE, 0);

        player.push(dashVelocity.x, dashVelocity.y, dashVelocity.z);

        player.hurtMarked = true;

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GOAT_RAM_IMPACT, SoundSource.PLAYERS, 0.5F, 1.2F);

        if (world.isClientSide) {
            for (int i = 0; i < 5; i++) {
                world.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        player.getX(), player.getY(0.5), player.getZ(),
                        -horizontalLook.x * 0.1, 0, -horizontalLook.z * 0.1);
            }
        }
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, MiztinkerHooks.LEFT_CLICK);
    }
}