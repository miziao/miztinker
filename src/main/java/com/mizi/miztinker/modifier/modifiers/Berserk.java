package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

/**
 * 狂暴（Berserk）特性：
 * 右键切换开启/关闭。
 * 开启时：
 *   - 造成400%伤害
 *   - 攻速+200%
 *   - 挖掘速度+300%
 *   - 每秒损失1耐久
 *   - 附带粒子特效
 */
public class Berserk extends NoLevelsModifier
        implements SlotStackModifierHook, InventoryTickModifierHook, MeleeDamageModifierHook {

    // 数据键：用于存储是否处于狂暴模式
    private static final ResourceLocation BERSERK_ACTIVE = new ResourceLocation("miztinker", "berserk_active");
    private static final int DURABILITY_LOSS_PER_SECOND = 1;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE);
    }

    /** 右键触发开关逻辑 */
    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry, ItemStack held, Slot slot, Player player, SlotAccess access) {
        ModDataNBT data = tool.getPersistentData();
        boolean nowActive = !data.getBoolean(BERSERK_ACTIVE);
        data.putBoolean(BERSERK_ACTIVE, nowActive);

        if (nowActive) {
            player.displayClientMessage(Component.literal("§c狂暴模式已开启！"), true);
        } else {
            player.displayClientMessage(Component.literal("§7狂暴模式已关闭。"), true);
        }
        return true;
    }

    /** 伤害倍率（400%） */
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context,
                                float baseDamage, float damage) {
        if (tool.getPersistentData().getBoolean(BERSERK_ACTIVE)) {
            return damage * 4.0f;
        }
        return damage;
    }

    /** 每tick检查：每秒掉耐久 + 加速效果 */
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level level, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !isCorrectSlot) return;
        if (!(holder instanceof Player player)) return;

        if (level.getGameTime() % 20 == 0) {
            ModDataNBT data = tool.getPersistentData();
            if (data.getBoolean(BERSERK_ACTIVE)) {
                // 每秒掉1耐久
                ToolDamageUtil.damage(tool, DURABILITY_LOSS_PER_SECOND, player, stack);

                // 强化效果
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 25, 3, false, false));  // 攻击
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 25, 3, false, false));     // 挖掘
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 2, false, false));// 移速

                // 生成红色狂暴粒子
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            4, 0.3, 0.4, 0.3, 0.01);
                }
            }
        }
    }
}