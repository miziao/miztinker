package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;

public class TrinketHate extends NoLevelsModifier implements InventoryTickModifierHook, OnAttackedModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        // 只有玩家会稳定触发这个逻辑
        if (!level.isClientSide && isCorrectSlot && holder instanceof Player) {
            this.executeAnnihilation(level, holder);
        }
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        LivingEntity wearer = context.getEntity();
        Level level = wearer.level;

        if (!level.isClientSide && !(wearer instanceof Player)) {
            this.executeAnnihilation(level, wearer);
        }
    }

    private void executeAnnihilation(Level level, LivingEntity sourceEntity) {
        double radius = 8.0;
        List<Player> players = level.getEntitiesOfClass(Player.class, sourceEntity.getBoundingBox().inflate(radius));

        for (Player target : players) {
            CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
                if (annihilateCurios(handler)) {
                    level.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            });
        }
    }

    private boolean annihilateCurios(ICuriosItemHandler handler) {
        boolean[] hasAnnihilated = {false};
        handler.getCurios().values().forEach(stacksHandler -> {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();

            for (int i = 0; i < stacks.getSlots(); i++) {
                if (!stacks.getStackInSlot(i).isEmpty()) {
                    stacks.setStackInSlot(i, ItemStack.EMPTY);
                    hasAnnihilated[0] = true;
                }
            }
            for (int i = 0; i < cosmetics.getSlots(); i++) {
                if (!cosmetics.getStackInSlot(i).isEmpty()) {
                    cosmetics.setStackInSlot(i, ItemStack.EMPTY);
                    hasAnnihilated[0] = true;
                }
            }
        });
        return hasAnnihilated[0];
    }
}