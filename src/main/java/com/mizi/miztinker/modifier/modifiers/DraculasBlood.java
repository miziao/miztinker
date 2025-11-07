package com.mizi.miztinker.modifier.modifiers;

import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class DraculasBlood extends NoLevelsModifier implements OnAttackedModifierHook {


    private static final int BASE_COOLDOWN = 2400; // 120秒，1秒=20tick
    private static final int COOLDOWN_PER_LEVEL = 160; // 每级减8秒

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier,
                           EquipmentContext context, EquipmentSlot slot,
                           DamageSource source, float amount, boolean isDirectDamage) {

        if (!(context.getEntity() instanceof Player player)) return;

        // 获取吸血鬼数据
        VampirePlayer vampire = VampirePlayer.get(player);
        int level = vampire.getLevel();
        if (level <= 0)return;  // 玩家不是吸血鬼

        // 冷却判定
        long now = player.level().getGameTime();
        long lastTrigger = player.getPersistentData().getLong("DraculaCooldown");
        int cooldown = Math.max(0, BASE_COOLDOWN - level * COOLDOWN_PER_LEVEL);
        if (now - lastTrigger < cooldown) return;  // 冷却中

        // 血量阈值判定
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (health / maxHealth > 0.1f) return; // 高于10%，不触发

        // 触发回血
        player.setHealth(maxHealth);
        player.level().broadcastEntityEvent(player, (byte)35); // 爱心粒子
        player.getPersistentData().putLong("DraculaCooldown", now);

    }

    
}