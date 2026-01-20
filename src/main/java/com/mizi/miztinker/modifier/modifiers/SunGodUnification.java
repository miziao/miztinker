package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class SunGodUnification extends NoLevelsModifier implements
        GeneralInteractionModifierHook,
        ToolStatsModifierHook,
        SlotStackModifierHook {

    private static final ResourceLocation SACRIFICED_HEALTH = new ResourceLocation("miztinker", "sacrificed_health");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT);
        hookBuilder.addHook(this, ModifierHooks.TOOL_STATS);
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (player.level().isClientSide) return InteractionResult.PASS;

        if (source == InteractionSource.RIGHT_CLICK && player.isCrouching() && !tool.isBroken()) {
            float currentHealth = player.getHealth();
            if (currentHealth > 1.0f) {
                float sacrificeAmount = currentHealth - 1.0f;

                player.setHealth(1.0f);

                ModDataNBT data = tool.getPersistentData();
                float oldSacrifice = data.getFloat(SACRIFICED_HEALTH);
                data.putFloat(SACRIFICED_HEALTH, oldSacrifice + sacrificeAmount);

                if (tool instanceof ToolStack toolStack) {
                    toolStack.rebuildStats();
                }

                player.displayClientMessage(Component.literal("§6「将我的生命力转化为神的攻击力！这就是拉的翼神龙第三个特殊能力！」"), true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        float sacrifice = context.getPersistentData().getFloat(SACRIFICED_HEALTH);
        if (sacrifice > 0) {
            ToolStats.ATTACK_DAMAGE.add(builder, sacrifice);
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry, ItemStack held, Slot slot, Player player, SlotAccess access) {
        if (player.level().isClientSide) return true;

        float totalAttack = tool.getStats().get(ToolStats.ATTACK_DAMAGE);
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);

        if (maxHealthAttr != null) {
            double currentMax = maxHealthAttr.getBaseValue();
            double newMax = currentMax + totalAttack;

            maxHealthAttr.setBaseValue(newMax);
            player.setHealth((float) newMax);

            slot.set(ItemStack.EMPTY);

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.5f);
            player.sendSystemMessage(Component.literal("§6「由于持有者的消失，神的攻击力将归还于我的肉体……」"));
            player.sendSystemMessage(Component.literal("§e「这就是太阳神合一！我已不再是人类，我即是神！」"));
        }

        return true;
    }
}