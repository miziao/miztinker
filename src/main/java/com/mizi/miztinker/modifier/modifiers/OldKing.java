package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.sounds.MiztinkerSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class OldKing extends NoLevelsModifier implements GeneralInteractionModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {

        if (source == InteractionSource.RIGHT_CLICK && !tool.isBroken()) {

            SoundEvent s = MiztinkerSounds.HHHHA.get();

            player.playSound(s, 1.0f, 1.0f);

            if (!player.level().isClientSide) {
                double range = 128.0D;
                AABB area = player.getBoundingBox().inflate(range);
                List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, area);

                for (Mob mob : mobs) {
                    if (!mob.getUUID().equals(player.getUUID())) {
                        mob.setTarget(player);
                    }
                }
                ToolDamageUtil.damageAnimated(tool, modifier.getLevel(), player, hand);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}