package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.List;

import static com.mizi.miztinker.miztinker.getResource;

public class Soulization_Armor extends NoLevelsModifier implements OnAttackedModifierHook, TooltipModifierHook, VolatileDataModifierHook {

    public static final ResourceLocation ATTACK_COUNT = getResource("soul_attack_count");
    public static final ResourceLocation EXTRA_SOUL_SLOTS = getResource("extra_soul_slots");
    public static final int THRESHOLD = 200;

    private static final String KEY_AWAKEN = "modifier.miztinker.soulization_armor.awaken";
    private static final String KEY_PROGRESS = "modifier.miztinker.soulization_armor.progress";
    private static final String KEY_TOTAL_AWAKENED = "modifier.miztinker.soulization_armor.total_awakened";

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
        hookBuilder.addHook(this, ModifierHooks.VOLATILE_DATA);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (!context.getEntity().level().isClientSide && context.getEntity() instanceof ServerPlayer player) {
            if (slotType.getType() != EquipmentSlot.Type.ARMOR) return;

            ModDataNBT data = tool.getPersistentData();
            int count = data.getInt(ATTACK_COUNT) + 1;

            if (count >= THRESHOLD) {
                data.putInt(ATTACK_COUNT, 0);

                int currentExtra = data.getInt(EXTRA_SOUL_SLOTS);
                data.putInt(EXTRA_SOUL_SLOTS, currentExtra + 1);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

                player.displayClientMessage(Component.translatable(KEY_AWAKEN), true);
            } else {
                data.putInt(ATTACK_COUNT, count);
            }
        }
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
        int extraSlots = context.getPersistentData().getInt(EXTRA_SOUL_SLOTS);
        if (extraSlots > 0) {
            volatileData.addSlots(SlotType.SOUL, extraSlots);
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltips, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        int count = tool.getPersistentData().getInt(ATTACK_COUNT);
        int totalAwakened = tool.getPersistentData().getInt(EXTRA_SOUL_SLOTS);

        tooltips.add(Component.translatable(KEY_PROGRESS, count, THRESHOLD));

        if (totalAwakened > 0) {
            tooltips.add(Component.translatable(KEY_TOTAL_AWAKENED, totalAwakened));
        }
    }
}