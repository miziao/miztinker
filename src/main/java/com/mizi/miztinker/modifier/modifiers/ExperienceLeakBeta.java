package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class ExperienceLeakBeta extends Modifier implements MeleeDamageModifierHook, InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, ModifierEntry entry, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && holder instanceof Player player && !player.getAbilities().instabuild) {
            int modifierLevel = entry.getLevel();
            if (player.totalExperience > 0) {
                player.giveExperiencePoints(-modifierLevel);
            }
        }
    }

    @Override
    public float getMeleeDamage(@NotNull IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity attacker = context.getAttacker();

        if (attacker instanceof Player player) {
            int totalXP = player.totalExperience;
            int modifierLevel = entry.getLevel();

            int digitCount = (totalXP <= 0) ? 1 : (int) (Math.log10(totalXP) + 1);

            float multiplier = (float) digitCount * modifierLevel;

            return damage * Math.max(1.0f, multiplier);
        }

        return damage;
    }
}