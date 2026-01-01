package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;

import java.util.List;
import javax.annotation.Nullable;

public class ScepterOfDivineSociety extends NoLevelsModifier
        implements BlockInteractionModifierHook, ProcessLootModifierHook, TooltipModifierHook {

    private static final ResourceLocation SAVED_LOOT = new ResourceLocation("miztinker", "scepter_loot_table");

    @Override
    protected void registerHooks(ModuleHookMap.Builder builder) {
        builder.addHook(this, ModifierHooks.BLOCK_INTERACT, ModifierHooks.PROCESS_LOOT, ModifierHooks.TOOLTIP);
    }
    @Override
    public InteractionResult afterBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK) return InteractionResult.PASS;

        Player player = context.getPlayer();
        if (player == null || !player.isCrouching()) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        ModDataNBT data = tool.getPersistentData();

        if (!(be instanceof RandomizableContainerBlockEntity chest)) {
            data.remove(SAVED_LOOT);
            player.displayClientMessage(Component.literal("§c已清除保存的战利品"), true);
            return InteractionResult.SUCCESS;
        }

        ResourceLocation loot = chest.lootTable;

        if (loot == null) {
            // 已开过或手动设置无 loot table → 清除
            data.remove(SAVED_LOOT);
            player.displayClientMessage(Component.literal("§c箱子无战利品，已清除保存内容"), true);
            return InteractionResult.SUCCESS;
        }

        // 正常保存（覆盖旧的）
        data.putString(SAVED_LOOT, loot.toString());
        player.displayClientMessage(Component.literal("§a已记录战利品：" + loot), true);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);

        return InteractionResult.SUCCESS;
    }

    /*------------------------------------------------------------*/
    /*  Tooltip 显示保存的 Loot Table                              */
    /*------------------------------------------------------------*/
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry entry, @Nullable Player player,
                           List<Component> tooltip, TooltipKey key, TooltipFlag flag) {
        ModDataNBT data = tool.getPersistentData();

        if (data.contains(SAVED_LOOT)) {
            tooltip.add(Component.literal("§6记录战利品："));
            tooltip.add(Component.literal(" §e" + data.getString(SAVED_LOOT)));
        } else {
            tooltip.add(Component.literal("§7未记录战利品"));
        }
    }

    /*------------------------------------------------------------*/
    /*   击杀怪物——根据保存的 loot table 添加额外战利品            */
    /*------------------------------------------------------------*/
    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot, LootContext context) {
        // 必须有伤害源（表示是击杀掉落）
        if (!context.hasParam(LootContextParams.DAMAGE_SOURCE)) {
            return;
        }

        ModDataNBT data = tool.getPersistentData();
        if (!data.contains(SAVED_LOOT)) return;

        String lootString = data.getString(SAVED_LOOT);
        ResourceLocation lootID = new ResourceLocation(lootString);

        // 获取服务器和战利品表
        ServerLevel level = context.getLevel();
        if (level.isClientSide()) return;

        LootTable lootTable = level.getServer().getLootData().getLootTable(lootID);

        // 获取目标实体位置
        Entity target = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (target == null) return;

        // 构建战利品上下文（使用 CHEST 参数集）
        var builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, target.position())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, target);

        // 生成战利品并添加到掉落列表
        var drops = lootTable.getRandomItems(builder.create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST));
        generatedLoot.addAll(drops);
    }
}