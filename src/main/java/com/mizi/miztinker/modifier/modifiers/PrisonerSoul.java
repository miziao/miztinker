package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

public class PrisonerSoul extends NoLevelsModifier
        implements BlockInteractionModifierHook, TooltipModifierHook, InventoryTickModifierHook {

    private static final ResourceLocation KEY_X = ResourceLocation.parse("prisoner_soul_x");
    private static final ResourceLocation KEY_Y = ResourceLocation.parse("prisoner_soul_y");
    private static final ResourceLocation KEY_Z = ResourceLocation.parse("prisoner_soul_z");
    private static final ResourceLocation KEY_BLOCK = ResourceLocation.parse("prisoner_soul_block");
    private static final ResourceLocation KEY_PROGRESS = ResourceLocation.parse("prisoner_soul_progress");

    @Override
    protected void registerHooks(ModuleHookMap.Builder builder) {
        builder.addHook(this, ModifierHooks.BLOCK_INTERACT, ModifierHooks.TOOLTIP, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public @NotNull InteractionResult afterBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK) return InteractionResult.PASS;

        Player player = context.getPlayer();
        if (player == null || !player.isCrouching()) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ModDataNBT data = tool.getPersistentData();

        if (state.is(Tags.Blocks.ORES)) {
            if (!level.isClientSide) {
                data.putInt(KEY_X, pos.getX());
                data.putInt(KEY_Y, pos.getY());
                data.putInt(KEY_Z, pos.getZ());

                ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                if (blockKey != null) {
                    data.putString(KEY_BLOCK, blockKey.toString());
                }

                data.putFloat(KEY_PROGRESS, 0.0f);

                player.displayClientMessage(Component.translatable("message.miztinker.prisoner_soul.captured"), true);
                level.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectStack, ItemStack stack) {
        if (level.isClientSide || holder.tickCount % 10 != 0) return;

        ModDataNBT data = tool.getPersistentData();
        if (!data.contains(KEY_BLOCK)) return;

        BlockPos pos = new BlockPos(data.getInt(KEY_X), data.getInt(KEY_Y), data.getInt(KEY_Z));
        BlockState state = level.getBlockState(pos);

        String savedBlockRes = data.getString(KEY_BLOCK);
        ResourceLocation currentRes = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String currentBlockRes = (currentRes != null) ? currentRes.toString() : "";

        if (!currentBlockRes.equals(savedBlockRes)) {
            clearData(data);
            return;
        }

        float miningSpeed = tool.getStats().get(ToolStats.MINING_SPEED);
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness <= 0) hardness = 1.0f;

        float currentProgress = data.getFloat(KEY_PROGRESS);
        currentProgress += (miningSpeed / hardness) * 0.05f;

        if (currentProgress >= 1.0f) {
            if (level instanceof ServerLevel serverLevel) {
                LootParams params = new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, stack)
                        .withParameter(LootContextParams.BLOCK_STATE, state)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, holder)
                        .create(LootContextParamSets.BLOCK);

                ResourceLocation lootTableLocation = state.getBlock().getLootTable();
                serverLevel.getServer().getLootData()
                        .getLootTable(lootTableLocation)
                        .getRandomItems(params)
                        .forEach(drop -> {
                            Block.popResource(serverLevel, pos.above(), drop);
                        });

                serverLevel.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 0.3f, 1.2f);
            }
            currentProgress = 0.0f;
        }

        data.putFloat(KEY_PROGRESS, currentProgress);
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry entry, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag flag) {
        ModDataNBT data = tool.getPersistentData();

        if (data.contains(KEY_BLOCK)) {
            ResourceLocation res = ResourceLocation.parse(data.getString(KEY_BLOCK));
            Block block = ForgeRegistries.BLOCKS.getValue(res);
            Component blockName = (block != null) ? block.getName() : Component.literal("Unknown");

            int x = data.getInt(KEY_X);
            int y = data.getInt(KEY_Y);
            int z = data.getInt(KEY_Z);
            int progress = (int)(data.getFloat(KEY_PROGRESS) * 100);

            tooltip.add(Component.translatable("tooltip.miztinker.prisoner_soul.block", blockName));
            tooltip.add(Component.translatable("tooltip.miztinker.prisoner_soul.pos", x, y, z));
            tooltip.add(Component.translatable("tooltip.miztinker.prisoner_soul.progress", progress));
        } else {
            tooltip.add(Component.translatable("tooltip.miztinker.prisoner_soul.empty"));
        }
    }

    private void clearData(ModDataNBT data) {
        data.remove(KEY_X);
        data.remove(KEY_Y);
        data.remove(KEY_Z);
        data.remove(KEY_BLOCK);
        data.remove(KEY_PROGRESS);
    }
}