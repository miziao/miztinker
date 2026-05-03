package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.UsingToolModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.function.Predicate;

public class DivineCrossbow extends NoLevelsModifier implements UsingToolModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_USING);
    }

    @Override
    public void onUsingTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int duration, int timeLeft, ModifierEntry activeModifier) {
        if (entity.level().isClientSide || !(entity instanceof Player player)) {
            return;
        }

        if (!(tool.getItem() instanceof ModifiableCrossbowItem crossbow)) {
            return;
        }

        ModDataNBT persistentData = tool.getPersistentData();
        int drawTime = persistentData.getInt(GeneralInteractionModifierHook.KEY_DRAWTIME);

        if (drawTime > 0 && (duration - timeLeft) >= drawTime) {

            InteractionHand hand = player.getUsedItemHand();
            Predicate<ItemStack> ammoPredicate = crossbow.getSupportedHeldProjectiles();

            if (!persistentData.contains(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO)) {
                ItemStack ammo = BowAmmoModifierHook.consumeAmmo(tool, player.getItemInHand(hand), player, player, ammoPredicate);

                if (!ammo.isEmpty()) {
                    CompoundTag ammoNBT = new CompoundTag();
                    ammo.save(ammoNBT);
                    persistentData.put(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO, ammoNBT);
                }
            }

            CompoundTag heldAmmo = persistentData.getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO);
            if (!heldAmmo.isEmpty()) {

                ModifiableCrossbowItem.fireCrossbow(tool, player, hand, heldAmmo);

                ItemStack nextAmmo = BowAmmoModifierHook.getAmmo(tool, player.getItemInHand(hand), player, ammoPredicate);

                if (!nextAmmo.isEmpty()) {
                    player.stopUsingItem();
                    GeneralInteractionModifierHook.startDrawtime(tool, player, 1.0f);
                    player.startUsingItem(hand);
                }
            }
        }
    }
}