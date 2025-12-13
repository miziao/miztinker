package com.mizi.miztinker.modifier.modifiers.base;


import com.mizi.miztinker.miztinker;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



@Mod.EventBusSubscriber(modid = miztinker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AbsoluteSeverance extends NoLevelsModifier implements MeleeHitModifierHook {
    //TODO 切断死目标凋落物
    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    private final float value;
    private final float baseDamage;

    public AbsoluteSeverance(float value, float baseDamage) {
        this.value = value;
        this.baseDamage = baseDamage;
    }

    private static final Map<UUID, Long> NULL_ENTITY_TIMES = new HashMap<>();


    /**
     * 手动触发 Advancements (成就)
     */
    public static void triggerKillAdvancement(LivingEntity target, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, target, source);
        }
    }

    /**
     * 确保实体被正确标记为死亡
     */
    public static void setEntityDead(LivingEntity entity) {
        try {
            Field deadField = ObfuscationReflectionHelper.findField(LivingEntity.class, "f_20890_"); // isDead
            deadField.setBoolean(entity, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理实体掉落的战利品
     */
    public static void dropLoot(LivingEntity entity, DamageSource ds) {
        try {
            Method dropAllDeathLootMethod = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_6668_", DamageSource.class);
            dropAllDeathLootMethod.invoke(entity, ds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
//        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }
}