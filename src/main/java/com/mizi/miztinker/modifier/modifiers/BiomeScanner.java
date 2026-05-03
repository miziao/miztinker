package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class BiomeScanner extends NoLevelsModifier implements GeneralInteractionModifierHook {

    public static final ResourceLocation STORED_BIOME_KEY = ResourceLocation.fromNamespaceAndPath("miztinker", "stored_biome_id");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public @NotNull InteractionResult onToolUse(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Player player, @NotNull InteractionHand hand, @NotNull InteractionSource source) {
        if (player.level().isClientSide || tool.isBroken() || source != InteractionSource.RIGHT_CLICK) {
            return InteractionResult.PASS;
        }

        if (player.isCrouching()) {
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();

            Holder<Biome> biomeHolder = level.getBiome(pos);
            ResourceLocation id = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biomeHolder.value());

            if (id != null) {
                tool.getPersistentData().putString(STORED_BIOME_KEY, id.toString());

                player.sendSystemMessage(Component.translatable("modifier.miztinker.biome_scanner.success")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(id.toString()).withStyle(ChatFormatting.AQUA)));

                level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}