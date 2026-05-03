package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
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

    private static final String KEY_ANNOUNCE = "modifier.miztinker.death_note.announce";
    private static final String KEY_SUMMARY = "modifier.miztinker.death_note.summary";
    private static final String KEY_TOOLTIP_COUNT = "modifier.miztinker.death_note.tooltip_count";
    private static final String KEY_TOOLTIP_EMPTY = "modifier.miztinker.death_note.tooltip_empty";

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
        String nameStr = stack.getHoverName().getString();
        ResourceLocation id = ResourceLocation.tryParse(nameStr);
        if (id == null) return InteractionResult.FAIL;

        EntityType<?> targetType = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (targetType == null) return InteractionResult.FAIL;

        ModDataNBT data = tool.getPersistentData();
        int currentKills = data.getInt(TAG_DEATH_COUNT);
        int killedThisTime = 0;

        Component targetDisplayName = targetType.getDescription();

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(512),
                e -> e.getType() == targetType && e.isAlive());

        if (!targets.isEmpty()) {
            player.displayClientMessage(Component.translatable(KEY_ANNOUNCE, targetDisplayName)
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);

            for (LivingEntity living : targets) {
                forceHurtWithNoHealable(living, level.damageSources().generic(), living.getHealth());
                forceSetAllCandidateHealth(living, 0F);
                killedThisTime++;
            }

            int totalKills = currentKills + killedThisTime;
            data.putInt(TAG_DEATH_COUNT, totalKills);

            player.displayClientMessage(Component.translatable(KEY_SUMMARY, killedThisTime, totalKills)
                    .withStyle(ChatFormatting.BLACK), true);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag flag) {
        int kills = tool.getPersistentData().getInt(TAG_DEATH_COUNT);

        if (kills > 0) {
            tooltip.add(Component.translatable(KEY_TOOLTIP_COUNT, kills)
                    .withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltip.add(Component.translatable(KEY_TOOLTIP_EMPTY)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}