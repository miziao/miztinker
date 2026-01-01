package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class StructureScanner extends NoLevelsModifier
        implements GeneralInteractionModifierHook {

    @Override
    public @NotNull InteractionResult onToolUse(
            @NotNull IToolStackView tool,
            @NotNull ModifierEntry modifier,
            Player player,
            @NotNull InteractionHand hand,
            @NotNull InteractionSource source
    ) {
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        if (source == InteractionSource.RIGHT_CLICK
                && player.isCrouching()
                && !tool.isBroken()) {

            ServerLevel level = (ServerLevel) player.level();

            BlockPos feetPos = player.blockPosition().below();

            var structures = level.structureManager()
                    .getAllStructuresAt(feetPos);

            Structure matchedStructure = null;

            for (Structure structure : structures.keySet()) {

                var start = level.structureManager()
                        .getStructureAt(feetPos, structure);

                if (!start.isValid()) continue;

                boolean onPiece = start.getPieces().stream()
                        .anyMatch(piece ->
                                piece.getBoundingBox().isInside(feetPos)
                        );

                if (onPiece) {
                    matchedStructure = structure;
                    break;
                }
            }


            if (matchedStructure == null) {
                player.sendSystemMessage(
                        Component.literal("§7当前未检测到结构")
                );
            } else {
                ResourceLocation id = level.registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                        .getKey(matchedStructure);

                if (id != null) {
                    Component clickable = Component.literal(id.toString())
                            .withStyle(style -> style
                                    .withColor(ChatFormatting.AQUA)
                                    .withClickEvent(new ClickEvent(
                                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                                            id.toString()
                                    ))
                                    .withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("点击复制结构ID")
                                    ))
                            );

                    player.sendSystemMessage(
                            Component.literal("§a当前结构：")
                                    .append(clickable)
                    );
                }
            }

            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }
}