package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;

public class Digitization extends NoLevelsModifier implements BlockInteractionModifierHook, GeneralInteractionModifierHook, TooltipModifierHook {

    private static final ResourceLocation STORED_STATE = ResourceLocation.parse("miztinker:stored_block_state");
    private static final ResourceLocation STORED_BE_NBT = ResourceLocation.parse("miztinker:stored_block_entity");
    private static final ResourceLocation STORED_FLUID = ResourceLocation.parse("miztinker:stored_fluid");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT, ModifierHooks.GENERAL_INTERACT, ModifierHooks.TOOLTIP);
    }


    @Override
    public @NotNull InteractionResult onToolUse(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Player player, @NotNull InteractionHand hand, @NotNull InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK || !player.isCrouching() || player.level().isClientSide) return InteractionResult.PASS;

        ModDataNBT data = tool.getPersistentData();
        if (!data.contains(STORED_STATE) && !data.contains(STORED_FLUID)) {
            BlockHitResult hit = getAdvancedHitResult(player.level(), player);
            if (hit.getType() == HitResult.Type.BLOCK) {
                return tryPickBlock(tool, player, player.level(), hit.getBlockPos());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK || !context.getPlayer().isCrouching()) return InteractionResult.PASS;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;

        ModDataNBT data = tool.getPersistentData();
        boolean hasData = data.contains(STORED_STATE) || data.contains(STORED_FLUID);

        if (hasData) {
            BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
            return tryPlaceBlock(tool, context.getPlayer(), context.getLevel(), placePos) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        } else {
            return tryPickBlock(tool, context.getPlayer(), context.getLevel(), context.getClickedPos());
        }
    }

    private InteractionResult tryPickBlock(IToolStackView tool, Player player, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluid = level.getFluidState(pos);
        ModDataNBT data = tool.getPersistentData();

        if (!fluid.isEmpty()) {
            ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid.getType());
            data.putString(STORED_FLUID, fluidId.toString());
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            playEffects(player, pos, "流体: " + fluidId.getPath());
            return InteractionResult.SUCCESS;
        }

        if (state.isAir()) return InteractionResult.PASS;

        data.put(STORED_STATE, NbtUtils.writeBlockState(state));
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            data.put(STORED_BE_NBT, be.saveWithFullMetadata());
            level.removeBlockEntity(pos);
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        playEffects(player, pos, state.getBlock().getName().getString());
        return InteractionResult.SUCCESS;
    }

    private boolean tryPlaceBlock(IToolStackView tool, Player player, Level level, BlockPos pos) {
        if (!level.getBlockState(pos).canBeReplaced()) return false;

        ModDataNBT data = tool.getPersistentData();

        if (data.contains(STORED_FLUID)) {
            ResourceLocation fluidId = ResourceLocation.parse(data.getString(STORED_FLUID));
            Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
            if (fluid != Fluids.EMPTY) {
                level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 3);
                level.neighborChanged(pos, fluid.defaultFluidState().createLegacyBlock().getBlock(), pos);
                data.remove(STORED_FLUID);
                playPlaceEffects(level, pos);
                return true;
            }
        }

        if (data.contains(STORED_STATE)) {
            BlockState state = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), data.getCompound(STORED_STATE));
            level.setBlock(pos, state, 3);
            if (data.contains(STORED_BE_NBT)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be != null) {
                    CompoundTag beTag = data.getCompound(STORED_BE_NBT);
                    beTag.putInt("x", pos.getX());
                    beTag.putInt("y", pos.getY());
                    beTag.putInt("z", pos.getZ());
                    be.load(beTag);
                }
            }
            data.remove(STORED_STATE);
            data.remove(STORED_BE_NBT);
            playPlaceEffects(level, pos);
            return true;
        }

        return false;
    }

    private void playEffects(Player player, BlockPos pos, String name) {
        player.displayClientMessage(Component.literal("§b已数据化: §f" + name), true);
        player.level().playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    private void playPlaceEffects(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.7f, 0.8f);
    }


    protected static BlockHitResult getAdvancedHitResult(Level level, Player player) {
        return level.clip(new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getViewVector(1.0F).scale(5.0D)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player));
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry entry, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag flag) {
        ModDataNBT data = tool.getPersistentData();
        if (data.contains(STORED_STATE)) {
            String name = data.getCompound(STORED_STATE).getString("Name").replace("minecraft:", "");
            tooltip.add(Component.literal("§d▶ 载入数据: §e" + name));
        } else if (data.contains(STORED_FLUID)) {
            String name = data.getString(STORED_FLUID).replace("minecraft:", "");
            tooltip.add(Component.literal("§b▶ 载入流体: §e" + name));
        } else {
            tooltip.add(Component.literal("§7▷ 缓存区空闲"));
        }
    }
}