package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
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

    public static final UUID HP_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID ATK_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    @Override
    protected void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (source != InteractionSource.RIGHT_CLICK || tool.isBroken()) return InteractionResult.PASS;
        GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onFinishUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
        if (entity instanceof Player player && !tool.isBroken()) {
            consumeTool(tool, player);
        }
    }

    private void consumeTool(IToolStackView tool, Player player) {
        float maxDur = tool.getStats().get(ToolStats.DURABILITY);
        int toDamage = Math.max(1, (int)maxDur / 10);

        if (ToolDamageUtil.damageAnimated(tool, toDamage, player, player.getUsedItemHand())) {
            player.broadcastBreakEvent(player.getUsedItemHand());
        }

        double gain = toDamage * 0.01;
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag mizData = persistentData.getCompound(Player.PERSISTED_NBT_TAG); // 获取 Forge 自动保留的 NBT 槽位

        double newHp = mizData.getDouble("miz_lp_hp") + gain;
        double newAtk = mizData.getDouble("miz_lp_atk") + gain;

        mizData.putDouble("miz_lp_hp", newHp);
        mizData.putDouble("miz_lp_atk", newAtk);
        persistentData.put(Player.PERSISTED_NBT_TAG, mizData);

        applyPermanentAttributes(player);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void applyPermanentAttributes(Player player) {
        CompoundTag mizData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);

        double hp = mizData.getDouble("miz_lp_hp");
        double atk = mizData.getDouble("miz_lp_atk");

        updateAttribute(player, Attributes.MAX_HEALTH, HP_UUID, "Lollipop HP", hp);
        updateAttribute(player, Attributes.ATTACK_DAMAGE, ATK_UUID, "Lollipop ATK", atk);
    }

    private static void updateAttribute(Player player, Attribute attr, UUID uuid, String name, double value) {
        if (value <= 0) return;
        AttributeInstance inst = player.getAttribute(attr);
        if (inst != null) {
            inst.removeModifier(uuid);
            inst.addTransientModifier(new AttributeModifier(uuid, name, value, AttributeModifier.Operation.ADDITION));
        }
    }

    @Override public UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) { return UseAnim.EAT; }
    @Override public int getUseDuration(IToolStackView tool, ModifierEntry modifier) { return 8; }
}