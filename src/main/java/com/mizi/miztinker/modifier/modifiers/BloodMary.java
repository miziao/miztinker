package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.hook.MiztinkerHooks;
import com.mizi.miztinker.modifier.hook.ArrowDamageModifierHook;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

public class BloodMary extends NoLevelsModifier implements MeleeDamageModifierHook, ArrowDamageModifierHook {

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry entry, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity attacker = context.getAttacker();
        LivingEntity target = context.getLivingTarget();
        if (target == null) return damage;

        if (attacker instanceof Player) {
            float lossHp = target.getMaxHealth() - target.getHealth();
            float damageBoost = (float) Math.pow(lossHp, 0.6);
            return damage + damageBoost;
        }
        return damage;
    }

    @Override
    public float getArrowDamage(ModDataNBT nbt, ModifierEntry entry, ModifierNBT modifierNBT, AbstractArrow arrow, @Nullable LivingEntity attacker, @NotNull Entity target, float basedamage, float damage) {
        if (attacker instanceof Player && target instanceof LivingEntity living) {
            float lossHp = living.getMaxHealth() - living.getHealth();
            float damageBoost = (float) Math.pow(lossHp, 0.6);
            return damage + damageBoost;
        }
        return damage;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
        hookBuilder.addHook(this, MiztinkerHooks.ARROW_DAMAGE);
    }
}