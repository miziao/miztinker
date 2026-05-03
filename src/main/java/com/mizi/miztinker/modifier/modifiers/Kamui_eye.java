package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerEffect;
import com.mizi.miztinker.sounds.MiztinkerSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import static com.mizi.miztinker.miztinker.getResource;

public class Kamui_eye extends NoLevelsModifier implements SlotStackModifierHook, DamageBlockModifierHook {

    public static final ResourceLocation KAMUI_ACTIVE_KEY = getResource("kamui_eye_active");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.SLOT_STACK);
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
    }

    @Override
    public boolean overrideOtherStackedOnMe(IToolStackView tool, ModifierEntry entry,
                                            ItemStack held, Slot slot, Player player, SlotAccess access) {

        MobEffectInstance currentEffect = player.getEffect(MiztinkerEffect.KAMUI_PLUS.get());

        if (currentEffect == null) {
            player.addEffect(new MobEffectInstance(MiztinkerEffect.KAMUI_PLUS.get(), 6000, 0, false, true));
            player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                    MiztinkerSounds.KAMUI.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            try {
                com.mizi.miztinker.effect.Pair_Kamui_effect.BYPASS_THREAD_LOCAL.set(true);

                player.removeEffect(MiztinkerEffect.KAMUI_PLUS.get());

                if (!player.level().isClientSide) {
                    com.mizi.miztinker.modifier.modifiers.base.ForceHurtUtil.recoverToNormalHealth(player);
                    tool.getPersistentData().putBoolean(KAMUI_ACTIVE_KEY, false);
                }

            } finally {
                com.mizi.miztinker.effect.Pair_Kamui_effect.BYPASS_THREAD_LOCAL.set(false);
            }
        }
        return true;
    }

    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry, EquipmentContext context,
                                   EquipmentSlot slot, DamageSource source, float damage) {
        return slot.getType() == EquipmentSlot.Type.ARMOR;
    }
}