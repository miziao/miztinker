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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.EntityInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class Digitization extends NoLevelsModifier
        implements BlockInteractionModifierHook, GeneralInteractionModifierHook,
        EntityInteractionModifierHook, TooltipModifierHook {

    private static final ResourceLocation STORED_STATE = ResourceLocation.parse("miztinker:stored_block_state");
    private static final ResourceLocation STORED_BE_NBT = ResourceLocation.parse("miztinker:stored_block_entity");
    private static final ResourceLocation STORED_FLUID = ResourceLocation.parse("miztinker:stored_fluid");
    private static final ResourceLocation STORED_ENTITY = ResourceLocation.parse("miztinker:stored_entity");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.BLOCK_INTERACT, ModifierHooks.GENERAL_INTERACT,
                ModifierHooks.ENTITY_INTERACT, ModifierHooks.TOOLTIP);
    }

    @Override
    public InteractionResult beforeEntityUse(IToolStackView tool, ModifierEntry modifier, Player player,
                                             Entity target, InteractionHand hand, InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK || !player.isCrouching()) return InteractionResult.PASS;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        ModDataNBT data = tool.getPersistentData();
        if (!hasStoredData(data) && !(target instanceof Player) && target instanceof LivingEntity) {
            return tryPickEntity(tool, player, target);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult onToolUse(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier,
                                                Player player, @NotNull InteractionHand hand, @NotNull InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK || !player.isCrouching() || player.level().isClientSide)
            return InteractionResult.PASS;

        ModDataNBT data = tool.getPersistentData();

        if (hasStoredData(data)) {
            BlockHitResult hit = getAdvancedHitResult(player.level(), player);
            BlockPos placePos = hit.getBlockPos().relative(hit.getDirection());
            return tryPlaceStored(tool, player, player.level(), placePos) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context,
                                            InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK || !context.getPlayer().isCrouching()) return InteractionResult.PASS;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;

        ModDataNBT data = tool.getPersistentData();
        if (hasStoredData(data)) {
            BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
            return tryPlaceStored(tool, context.getPlayer(), context.getLevel(), placePos)
                    ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        } else {
            return tryPickBlock(tool, context.getPlayer(), context.getLevel(), context.getClickedPos());
        }
    }

    private boolean hasStoredData(ModDataNBT data) {
        return data.contains(STORED_STATE) || data.contains(STORED_FLUID) || data.contains(STORED_ENTITY);
    }

    private InteractionResult tryPickEntity(IToolStackView tool, Player player, Entity entity) {
        CompoundTag entityTag = new CompoundTag();
        if (entity.saveAsPassenger(entityTag)) {
            tool.getPersistentData().put(STORED_ENTITY, entityTag);
            entity.discard();
            playEffects(player, entity.blockPosition(), "entity");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private InteractionResult tryPickBlock(IToolStackView tool, Player player, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        FluidState fluid = level.getFluidState(pos);
        ModDataNBT data = tool.getPersistentData();

        if (!fluid.isEmpty() && fluid.isSource()) {
            data.putString(STORED_FLUID, BuiltInRegistries.FLUID.getKey(fluid.getType()).toString());
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            playEffects(player, pos, "fluid");
            return InteractionResult.SUCCESS;
        }

        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) return InteractionResult.PASS;

        data.put(STORED_STATE, NbtUtils.writeBlockState(state));
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            data.put(STORED_BE_NBT, be.saveWithFullMetadata());
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        playEffects(player, pos, "block");
        return InteractionResult.SUCCESS;
    }

    private boolean tryPlaceStored(IToolStackView tool, Player player, Level level, BlockPos pos) {
        ModDataNBT data = tool.getPersistentData();

        if (data.contains(STORED_ENTITY)) {
            CompoundTag nbt = data.getCompound(STORED_ENTITY);
            return EntityType.create(nbt, level).map(entity -> {
                entity.load(nbt);
                entity.setUUID(UUID.randomUUID());
                entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                level.addFreshEntity(entity);
                data.remove(STORED_ENTITY);
                playPlaceEffects(level, pos);
                return true;
            }).orElse(false);
        }

        if (data.contains(STORED_FLUID)) {
            Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(data.getString(STORED_FLUID)));
            if (fluid != Fluids.EMPTY) {
                level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), 3);
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
                    beTag.putInt("x", pos.getX()); beTag.putInt("y", pos.getY()); beTag.putInt("z", pos.getZ());
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

    private void playEffects(Player player, BlockPos pos, String type) {
        player.displayClientMessage(Component.translatable("message.miztinker.digitization.stored",
                Component.translatable("message.miztinker.digitization.type." + type)), true);
        player.level().playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    private void playPlaceEffects(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.7f, 0.8f);
    }

    protected static BlockHitResult getAdvancedHitResult(Level level, Player player) {
        return level.clip(new ClipContext(player.getEyePosition(),
                player.getEyePosition().add(player.getViewVector(1.0F).scale(5.0D)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry entry, @Nullable Player player,
                           List<Component> tooltip, TooltipKey key, TooltipFlag flag) {
        ModDataNBT data = tool.getPersistentData();
        if (data.contains(STORED_ENTITY)) {
            String id = data.getCompound(STORED_ENTITY).getString("id");
            tooltip.add(Component.translatable("tooltip.miztinker.digitization.entity", Component.translatable(EntityType.byString(id).get().getDescriptionId())));
        } else if (data.contains(STORED_STATE)) {
            String name = data.getCompound(STORED_STATE).getString("Name");
            tooltip.add(Component.translatable("tooltip.miztinker.digitization.block", Component.translatable(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(name)).getDescriptionId())));
        } else if (data.contains(STORED_FLUID)) {
            String id = data.getString(STORED_FLUID);
            tooltip.add(Component.translatable("tooltip.miztinker.digitization.fluid", Component.translatable(BuiltInRegistries.FLUID.get(ResourceLocation.parse(id)).getFluidType().getDescriptionId())));
        } else {
            tooltip.add(Component.translatable("tooltip.miztinker.digitization.empty"));
        }
    }
}