package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.RequirementsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.BitSet;
import java.util.List;

public class BiomeTerraformer extends NoLevelsModifier implements GeneralInteractionModifierHook, TooltipModifierHook, RequirementsModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT, ModifierHooks.TOOLTIP, ModifierHooks.REQUIREMENTS);
    }

    @Nullable
    @Override
    public Component requirementsError(ModifierEntry entry) {
        return Component.translatable("modifier.miztinker.biome_terraformer.requirements");
    }

    @Override
    public void addTooltip(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, @Nullable Player player, @NotNull List<Component> tooltip, @NotNull TooltipKey tooltipKey, @NotNull TooltipFlag tooltipFlag) {
        String storedId = tool.getPersistentData().getString(BiomeScanner.STORED_BIOME_KEY);
        if (!storedId.isEmpty()) {
            ResourceLocation id = ResourceLocation.parse(storedId);
            tooltip.add(Component.translatable("modifier.miztinker.biome_terraformer.current_biome")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("biome." + id.getNamespace() + "." + id.getPath()).withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.translatable("modifier.miztinker.biome_terraformer.no_data").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public @NotNull InteractionResult onToolUse(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Player player, @NotNull InteractionHand hand, @NotNull InteractionSource source) {
        if (player.level().isClientSide || player.isCrouching() || tool.isBroken() || source != InteractionSource.RIGHT_CLICK) {
            return InteractionResult.PASS;
        }

        String storedIdStr = tool.getPersistentData().getString(BiomeScanner.STORED_BIOME_KEY);
        if (storedIdStr.isEmpty()) return InteractionResult.FAIL;

        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = player.blockPosition();
        ResourceLocation storedId = ResourceLocation.parse(storedIdStr);

        Holder<Biome> targetToReplace = level.getBiome(pos);

        level.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolder(ResourceKey.create(Registries.BIOME, storedId))
                .ifPresent(newBiome -> {
                    LevelChunk chunk = level.getChunkAt(pos);
                    replaceMatchingBiomesInChunk(chunk, targetToReplace, newBiome);

                    syncChunk(level, chunk);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.5F);
                });

        return InteractionResult.SUCCESS;
    }


    private void replaceMatchingBiomesInChunk(LevelChunk chunk, Holder<Biome> targetToReplace, Holder<Biome> newBiome) {
        if (targetToReplace.equals(newBiome)) return;

        for (LevelChunkSection section : chunk.getSections()) {
            if (section.getBiomes() instanceof PalettedContainer<Holder<Biome>> container) {
                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        for (int z = 0; z < 4; z++) {
                            if (container.get(x, y, z).equals(targetToReplace)) {
                                container.set(x, y, z, newBiome);
                            }
                        }
                    }
                }
            }
        }
        chunk.setUnsaved(true);
    }

    private void syncChunk(ServerLevel level, LevelChunk chunk) {
        BitSet bitset = new BitSet();
        bitset.set(0, level.getSectionsCount());
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), bitset, bitset);
        for (ServerPlayer player : level.players()) {
            if (level.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(player.chunkPosition().toLong())) {
                player.connection.send(packet);
            }
        }
    }
}