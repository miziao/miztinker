package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.HashSet;
import java.util.Set;

import static com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.forceHurtWithNoHealable;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.forceSetAllCandidateHealth;

public class Death_Note_KingMode extends NoLevelsModifier implements GeneralInteractionModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide || source != InteractionSource.RIGHT_CLICK || !player.isCrouching() || tool.isBroken())
            return InteractionResult.PASS;

        if (!(player.level() instanceof ServerLevel level)) return InteractionResult.PASS;

        Set<EntityType<?>> targets = new HashSet<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.is(Items.WRITABLE_BOOK)) {
                CompoundTag nbt = invStack.getTag();
                if (nbt != null && nbt.contains("pages", Tag.TAG_LIST)) {
                    ListTag pages = nbt.getList("pages", Tag.TAG_STRING);
                    for (int j = 0; j < pages.size(); j++) {
                        for (String line : pages.getString(j).split("\n")) {
                            ResourceLocation rl = ResourceLocation.tryParse(line.trim());
                            if (rl != null) BuiltInRegistries.ENTITY_TYPE.getOptional(rl).ifPresent(targets::add);
                        }
                    }
                }
            }
        }

        if (targets.isEmpty()) {
            player.displayClientMessage(Component.literal("§c未发现有效名单"), true);
            return InteractionResult.FAIL;
        }

        boolean killedAny = false;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(512))) {
            if (targets.contains(living.getType()) && living.isAlive()) {
                forceHurtWithNoHealable(living, level.damageSources().generic(), living.getHealth());
                forceSetAllCandidateHealth(living, 0F);
                killedAny = true;
            }
        }

        if (killedAny) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN, SoundSource.PLAYERS, 1.0F, 0.8F);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }
}