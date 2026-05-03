package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import com.mizi.miztinker.modifier.hook.LeftClickModifierHook;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.DisplayNameModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.tools.TinkerModifiers;

import javax.annotation.Nullable;
import java.util.List;

public class MoonSlasher extends NoLevelsModifier implements LeftClickModifierHook, GeneralInteractionModifierHook, TooltipModifierHook, DisplayNameModifierHook, BlockBreakModifierHook {

    private static final ResourceLocation FORTUNE_MODE = ResourceLocation.fromNamespaceAndPath("miztinker", "fortune_mode");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, MiztinkerHooks.LEFT_CLICK);
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
        hookBuilder.addHook(this, ModifierHooks.DISPLAY_NAME);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_BREAK);
    }

    @Override
    public void onLeftClickBlock(IToolStackView tool, ModifierEntry entry, Player player, Level level, EquipmentSlot equipmentSlot, BlockState state, BlockPos pos) {
        if (level.isClientSide || state.isAir()) return;
        boolean isFortune = tool.getPersistentData().getBoolean(FORTUNE_MODE);
        if (isFortune) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack drop = new ItemStack(state.getBlock());
        if (blockEntity != null) {
            CompoundTag nbt = blockEntity.saveWithFullMetadata();
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("BlockEntityTag", nbt);
            drop.setTag(compoundTag);
            if (blockEntity instanceof net.minecraft.world.Container container) {
                container.clearContent();
            }
        }

        if (!drop.isEmpty()) {
            level.levelEvent(2001, pos, Block.getId(state));
            spawnStack(level, pos, drop);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.5F);
        }
    }


    @Override
    public @NotNull Component getDisplayName(int level) {
        return Component.translatable("modifier.miztinker.moon_slasher.fortune_name")
                .withStyle(style -> style.withColor(0xFFD700));
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        boolean isFortune = tool.getPersistentData().getBoolean(FORTUNE_MODE);
        int colorRgb = isFortune ? 0x555555 : 0xFFD700;

        if (tooltipKey == TooltipKey.SHIFT && !tooltip.isEmpty()) {
            MutableComponent baseToolName = Component.translatable(tool.getItem().getDescriptionId());
            MutableComponent paradoxName = Component.translatable("material.miztinker.paradox_manyullyn.inventory_name")
                    .append(baseToolName)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(colorRgb)));
            tooltip.set(0, paradoxName);
        }

        Component modeDesc = isFortune ?
                Component.translatable("modifier.miztinker.moon_slasher.fortune").withStyle(ChatFormatting.GOLD) :
                Component.translatable("modifier.miztinker.moon_slasher.silk").withStyle(ChatFormatting.DARK_GRAY);

        tooltip.add(Component.translatable("modifier.miztinker.moon_slasher.mode").append(modeDesc));
    }

    @Override
    public @NotNull Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
        boolean isFortune = tool.getPersistentData().getBoolean(FORTUNE_MODE);
        int colorRgb = isFortune ? 0x555555 : 0xFFD700;
        String nameKey = isFortune ? "modifier.miztinker.moon_slasher.fortune_name" : "modifier.miztinker.moon_slasher.silk_name";
        return Component.translatable(nameKey).withStyle(style -> style.withColor(TextColor.fromRgb(colorRgb)));
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (!tool.getPersistentData().getBoolean(FORTUNE_MODE)) return;

        int expanded = tool.getModifierLevel(TinkerModifiers.expanded.getId());
        if (expanded <= 0) return;

        ServerLevel world = context.getWorld();
        Player player = context.getPlayer();
        if (player == null) return;

        BlockPos origin = context.getPos();
        Direction side = context.getSideHit();

        if (expanded == 1) {
            breakExtraBlock(world, origin.below(), player);
        } else {
            int range = expanded / 2;
            int start = -range;
            int end = (expanded % 2 == 0) ? (range - 1) : range;

            for (int a = start; a <= end; a++) {
                for (int b = start; b <= end; b++) {
                    if (a == 0 && b == 0) continue;

                    BlockPos target;
                    if (side.getAxis() == Direction.Axis.Y) {
                        target = origin.offset(a, 0, b);
                    } else if (side.getAxis() == Direction.Axis.X) {
                        target = origin.offset(0, a, b);
                    } else {
                        target = origin.offset(a, b, 0);
                    }
                    breakExtraBlock(world, target, player);
                }
            }
        }
    }

    private void breakExtraBlock(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0) {
            level.destroyBlock(pos, true, player);
        }
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (player.isCrouching() && source == InteractionSource.RIGHT_CLICK) {
            if (!player.level().isClientSide) {
                boolean nextMode = !tool.getPersistentData().getBoolean(FORTUNE_MODE);
                tool.getPersistentData().putBoolean(FORTUNE_MODE, nextMode);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        nextMode ? SoundEvents.ENDER_EYE_DEATH : SoundEvents.SCULK_CLICKING,
                        SoundSource.PLAYERS, 1.0F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void spawnStack(Level level, BlockPos pos, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            ItemEntity itemEntity = new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            itemEntity.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(itemEntity);
        }
    }
}