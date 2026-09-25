package com.mizi.miztinker.modifier.modifiers;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class Wall_of_Skeleton extends NoLevelsModifier implements InventoryTickModifierHook {


    private static final int CHECK_INTERVAL = 10;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !isCorrectSlot) return;
        if (!(holder instanceof Player player)) return;

        if (player.tickCount % CHECK_INTERVAL != 0) return;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData == null) return;

        float currentMana = magicData.getMana();
        float maxMana = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA.get());

        if (currentMana < maxMana) {
            MobEffectInstance resistance = new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    40,
                    4,
                    true, false, true
            );
            player.addEffect(resistance);
        }
    }
}
