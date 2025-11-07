package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import com.yellowbrossproductions.yellowbrossextras.entities.DefenderEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.RequirementsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import lombok.Getter;

import static com.mizi.miztinker.modifier.modifiers.base.AbsoluteSeverance.*;
import static com.mizi.miztinker.modifier.modifiers.base.LivingEntityUtil.forceSetAllCandidateHealth;


@Getter
public class AwakenDoomGuy extends NoLevelsModifier implements
        MeleeHitModifierHook,
        InventoryTickModifierHook,
        RequirementsModifierHook,
        ValidateModifierHook,
        DamageBlockModifierHook {


    private static final int EFFECT_DURATION = 100; // 药水持续时间（tick）


    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
    }

    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry, EquipmentContext context,
                                   EquipmentSlot slot, DamageSource source, float damage) {
        return damage >= 0;
    }

    /* ===== 条件验证 ===== */
    @Override
    public @Nullable Component validate(IToolStackView tool, ModifierEntry entry) {
        if (tool.getModifierLevel(MiztinkerModifiers.DOOM_GUY.getId()) > 0) return null;
        return requirementsError(entry);
    }

    @Override
    public Component requirementsError(ModifierEntry entry) {
        return Component.translatable("modifier.miztinker.awakendoomguy.requirements");
    }

    /* ===== 攻击逻辑 ===== */
    @Override
    public float beforeMeleeHit(IToolStackView tool, ModifierEntry entry,
                                ToolAttackContext context, float damage,
                                float baseKnockback, float knockback) {

        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();

        if (target == null || player == null) return knockback;
        if (target.getHealth() <= 0 || isFromDummmmmmyMod(target) || isDefender(target)) return knockback;

        applyHalfMaxHealthDamage(target, player);

        return knockback;
    }

    @Override
    public void failedMeleeHit(IToolStackView tool, ModifierEntry modifier,
                               ToolAttackContext context, float damageAttempted) {

        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();

        if (target == null || player == null) return;
        if (target.getHealth() <= 0 || isFromDummmmmmyMod(target) || isDefender(target)) return;

        applyHalfMaxHealthDamage(target, player);
    }

    /* ===== 持续药水效果 ===== */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier,
                                Level level, LivingEntity living,
                                int slot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {

        if (!(living instanceof Player player)) return;

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION, 3, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, EFFECT_DURATION, 1, true, false, false));
    }

    /* ===== 半血伤害方法 ===== */
    public static void applyHalfMaxHealthDamage(LivingEntity target, Player player) {
        if (target.getHealth() <= 0) return;

        var playerKill = target.level().damageSources().playerAttack(player);

        // 先造成 1 点伤害保证触发事件
        target.hurt(playerKill, 1);

        // 剩余生命值 = 当前生命 - 最大生命的一半 - 1%
        float reHealth = target.getHealth() - target.getMaxHealth() / 2f - target.getMaxHealth() * 0.01f;

        forceSetAllCandidateHealth(target, reHealth);

        // OmniMod 兼容
        if (isFromOmniMod(target)) {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("Health", reHealth);
            try {
                target.readAdditionalSaveData(tag);
            } catch (Exception ignored) {}
        }

        // 死亡判定
        if (reHealth <= 0 || target.getHealth() <= 0) {
            forceSetAllCandidateHealth(target, 0);
            triggerKillAdvancement(target, playerKill);
            setEntityDead(target);
            dropLoot(target, playerKill);
        }
    }

    /* ===== 工具方法 ===== */
    public static boolean isFromDummmmmmyMod(Entity entity) {
        if (entity == null) return false;
        ResourceLocation entityId = EntityType.getKey(entity.getType());
        if (entityId != null && entityId.getNamespace().equals("dummmmmmy")) return true;
        return entity.getClass().getName().contains("dummmmmmy");
    }

    public static boolean isDefender(Entity entity) {
        if (entity instanceof DefenderEntity defender) {
            return defender.getPhase() == 0;
        }
        return false;
    }

    public static boolean isFromOmniMod(Entity entity) {
        if (entity == null) return false;
        return entity.getClass().getName().contains("omnimobs");
    }

}