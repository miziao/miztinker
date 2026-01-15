package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class ExperienceLeakGamma extends Modifier implements DamageBlockModifierHook, InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && holder instanceof Player player && !player.getAbilities().instabuild) {
            if (player.totalExperience > 0) {
                player.giveExperiencePoints(-entry.getLevel());
            }
        }
    }

    @Override
    public boolean isDamageBlocked(@NotNull IToolStackView tool, ModifierEntry entry, EquipmentContext context, EquipmentSlot slot, DamageSource source, float damage) {
        LivingEntity entity = context.getEntity();
        if (!(entity instanceof Player player)) return false;

        int totalModifierLevel = 0;

        for (EquipmentSlot armorSlot : EquipmentSlot.values()) {
            if (armorSlot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armorStack = player.getItemBySlot(armorSlot);
                if (!armorStack.isEmpty()) {
                    try {
                        // 使用 copyFrom 确保读取安全
                        ToolStack armorTool = ToolStack.copyFrom(armorStack);
                        totalModifierLevel += armorTool.getModifierLevel(this);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (totalModifierLevel > 0) {
            int playerLevel = player.experienceLevel;
            float totalProtection = (float) playerLevel * totalModifierLevel;

            if (damage <= totalProtection) {
                if (!player.level().isClientSide) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_BOTTLE_THROW, SoundSource.PLAYERS,
                            0.5F, 0.4F / (player.level().getRandom().nextFloat() * 0.4F + 0.8F));

                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                            0.3F, 0.5F);
                }
                return true;
            }
        }

        return false;
    }
}