package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import com.mizi.miztinker.network.MiztinkerNetwork;
import com.mizi.miztinker.network.TimeStopPacket;
import com.mizi.miztinker.util.MizTimeStopHandler;
import com.mizi.miztinker.util.Time;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.RequirementsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

public class AwakenKnight_of_Night extends NoLevelsModifier implements
        GeneralInteractionModifierHook,
        RequirementsModifierHook,
        ValidateModifierHook {

    private static final ResourceLocation ACTIVE = ResourceLocation.fromNamespaceAndPath("miztinker", "timestop_active");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.REQUIREMENTS);
        hookBuilder.addHook(this, ModifierHooks.VALIDATE);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier,
                                       Player player, InteractionHand hand,
                                       InteractionSource source) {

        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        if (source != InteractionSource.RIGHT_CLICK || !player.isCrouching() || tool.isBroken())
            return InteractionResult.PASS;

        if (player.getCooldowns().isOnCooldown(tool.getItem())) return InteractionResult.FAIL;
        player.getCooldowns().addCooldown(tool.getItem(), 20);

        ModDataNBT data = tool.getPersistentData();
        boolean nowActive = !data.getBoolean(ACTIVE);
        data.putBoolean(ACTIVE, nowActive);

        MizTimeStopHandler.toggle(player, nowActive);

        MiztinkerNetwork.sendToClient(new TimeStopPacket(nowActive));

        if (nowActive) {
            player.sendSystemMessage(Component.translatable("message.miztinker.timestop.active"));
        } else {
            player.sendSystemMessage(Component.translatable("message.miztinker.timestop.resume"));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable Component validate(IToolStackView tool, ModifierEntry entry) {
        if (tool.getModifierLevel(MiztinkerModifiers.KNIGHT_OF_NIGHT.getId()) > 0)
            return null;
        return requirementsError(entry);
    }

    @Override
    public Component requirementsError(ModifierEntry entry) {
        return Component.translatable("modifier.miztinker.awakenknight_of_night.requirements");
    }
}