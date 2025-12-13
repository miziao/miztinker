package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.UUID;

public class SuperLollipop extends NoLevelsModifier implements GeneralInteractionModifierHook {

    // 用于标识生命值加成的 UUID
    private static final UUID SUPER_LOLLIPOP_HEALTH_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Override
    protected void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public int getPriority() {
        // 高于默认的美味优先级
        return 100;
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        // 工具已损坏或者当前耐久为0，无法使用
        if (source != InteractionSource.RIGHT_CLICK || tool.isBroken() || tool.getCurrentDurability() <= 0) {
            return InteractionResult.PASS;
        }

        GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onFinishUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
        if (tool.isBroken() || !(entity instanceof Player)) return;

        Player player = (Player) entity;
        consumeTool(tool, modifier, player);
    }

    /** 消耗最大耐久的十分之一，并从这部分耐久计算增加百分之一为最大生命值 */
    public static void addHealth(Player player, double healthGain) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null) return;

        // 获取旧数值
        double oldValue = 0;
        AttributeModifier old = healthAttr.getModifier(SUPER_LOLLIPOP_HEALTH_UUID);

        if (old != null) {
            oldValue = old.getAmount();
            healthAttr.removeModifier(old);
        }

        // 叠加
        double newValue = oldValue + healthGain;

        // 重加永久 modifier
        healthAttr.addPermanentModifier(new AttributeModifier(
                SUPER_LOLLIPOP_HEALTH_UUID,
                "super_lollipop_health_boost",
                newValue,
                AttributeModifier.Operation.ADDITION
        ));

        // 立即更新血量
        player.setHealth(player.getMaxHealth());
    }



    /** 消耗工具的耐久并转化为生命值 **/
    private void consumeTool(IToolStackView tool, ModifierEntry modifier, Player player) {
        Level world = player.level();

        // 工具最大耐久
        float maxDurabilityFloat = tool.getStats().get(ToolStats.DURABILITY);

        if (Float.isInfinite(maxDurabilityFloat) || Float.isNaN(maxDurabilityFloat) || maxDurabilityFloat <= 0f) {
            maxDurabilityFloat = tool.getCurrentDurability();
        }

        int maxDurability = Math.max(1, (int)Math.floor(maxDurabilityFloat));

        // 消耗最大耐久的 10%
        int durabilityToConsume = Math.max(1, maxDurability / 10);
        int currentDur = tool.getCurrentDurability();

        if (currentDur <= 0) return;
        durabilityToConsume = Math.min(durabilityToConsume, currentDur);

        if (durabilityToConsume <= 0) return;

        // 造成耐久损伤
        if (ToolDamageUtil.damageAnimated(tool, durabilityToConsume, player, player.getUsedItemHand())) {
            player.broadcastBreakEvent(player.getUsedItemHand());
            return;
        }

        // 1% 的耐久转换为 HP
        float healthGain = durabilityToConsume * 0.01f;

        // 增加永久生命
        addHealth(player, healthGain);

        // 播放音效
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                1.0f, 1.0f);
    }

    @Override
    public UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
        return 8; // 使用持续时间（可以改短，但16和食物一致）
    }
}