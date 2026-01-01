package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Undone extends Modifier implements MeleeHitModifierHook {

    private static final Random RANDOM = new Random();

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();

        Level level = context.getAttacker().level();

        if (target == null || !target.isAlive() || level.isClientSide) {
            return;
        }

        float chance = modifier.getLevel() * 0.3f;
        if (RANDOM.nextFloat() > chance) {
            return;
        }

        List<EquipmentSlot> filledSlots = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!target.getItemBySlot(slot).isEmpty()) {
                filledSlots.add(slot);
            }
        }

        if (!filledSlots.isEmpty()) {
            EquipmentSlot randomSlot = filledSlots.get(RANDOM.nextInt(filledSlots.size()));
            ItemStack itemToDrop = target.getItemBySlot(randomSlot).copy();

            target.setItemSlot(randomSlot, ItemStack.EMPTY);

            ItemEntity itemEntity = new ItemEntity(level,
                    target.getX(), target.getY() + 0.5, target.getZ(),
                    itemToDrop);

            itemEntity.setPickUpDelay(20);
            level.addFreshEntity(itemEntity);

            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 1.0f, 0.5f);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
    }
}