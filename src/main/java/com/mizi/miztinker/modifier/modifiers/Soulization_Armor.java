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


public class Soulization_Armor extends NoLevelsModifier implements OnAttackedModifierHook, VolatileDataModifierHook, TooltipModifierHook {

    public static final ResourceLocation ATTACK_COUNT = getResource("soul_attack_count");
    public static final ResourceLocation EXTRA_SOULS = getResource("extra_souls");
    public static final int THRESHOLD = 200;

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
        hookBuilder.addHook(this, ModifierHooks.VOLATILE_DATA);
        hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        if (!context.getEntity().level().isClientSide && context.getEntity() instanceof ServerPlayer player) {

            ModDataNBT data = tool.getPersistentData();
            int count = data.getInt(ATTACK_COUNT) + 1;

            if (count >= THRESHOLD) {
                data.putInt(ATTACK_COUNT, 0);
                int currentSouls = data.getInt(EXTRA_SOULS);
                data.putInt(EXTRA_SOULS, currentSouls + 1);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.displayClientMessage(Component.literal("§b[魂质] 特性觉醒：额外灵魂槽 +1"), true);
            } else {
                data.putInt(ATTACK_COUNT, count);
            }
        }
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT nbt) {
        ModDataNBT persistentData = (ModDataNBT) context.getPersistentData();
        if (persistentData.contains(EXTRA_SOULS)) {
            nbt.addSlots(SlotType.SOUL, persistentData.getInt(EXTRA_SOULS));
        }
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltips, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        ModDataNBT data = tool.getPersistentData();
        int count = data.getInt(ATTACK_COUNT);
        int souls = data.getInt(EXTRA_SOULS);

        if (souls > 0) {
            tooltips.add(Component.literal("§7已获得额外灵魂槽: §b" + souls));
        }
        tooltips.add(Component.literal("§7魂质觉醒进度: §a" + count + " §7/ §e" + THRESHOLD));
    }
}
