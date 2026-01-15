package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;

import static com.mizi.miztinker.miztinker.getResource;
import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurtWithNoHealable;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.forceSetAllCandidateHealth;

public class Death_Note extends NoLevelsModifier implements GeneralInteractionModifierHook, TooltipModifierHook {

    public static final ResourceLocation TAG_DEATH_COUNT = getResource("death_count");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide) return InteractionResult.PASS;
        if (source != InteractionSource.RIGHT_CLICK || !player.isCrouching() || tool.isBroken()) return InteractionResult.PASS;

        if (!(player.level() instanceof ServerLevel level)) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        String name = stack.getHoverName().getString();
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null) return InteractionResult.FAIL;

        var targetType = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (targetType == null) return InteractionResult.FAIL;

        ModDataNBT data = tool.getPersistentData();
        int currentKills = data.getInt(TAG_DEATH_COUNT);
        int killedThisTime = 0;

        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(512))) {
            if (living.getType() != targetType || !living.isAlive()) continue;

            forceHurtWithNoHealable(living, level.damageSources().generic(), living.getHealth());
            forceSetAllCandidateHealth(living, 0F);

            killedThisTime++;
        }

        if (killedThisTime > 0) {
            data.putInt(TAG_DEATH_COUNT, currentKills + killedThisTime);

            player.displayClientMessage(Component.literal(
                    String.format("§0死亡笔记已新增 §4%d §0个死亡名单 (累计: %d)", killedThisTime, currentKills + killedThisTime)
            ), true);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag flag) {
        int kills = tool.getPersistentData().getInt(TAG_DEATH_COUNT);

        if (kills > 0) {
            tooltip.add(Component.literal("笔记中已记录的名字: ").withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal(String.valueOf(kills)).withStyle(ChatFormatting.RED)));
        } else {
            tooltip.add(Component.literal("尚未记录任何灵魂...").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}