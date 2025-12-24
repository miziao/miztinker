package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pyre.tinkerslevellingaddon.ImprovableModifier;
import pyre.tinkerslevellingaddon.config.Config;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.Random;

public class Double_Evolution_Pill extends NoLevelsModifier implements VolatileDataModifierHook, InventoryTickModifierHook {

    private static final ResourceLocation PERSIST_UPGRADES = new ResourceLocation("miztinker", "extra_upgrades");
    private static final ResourceLocation PERSIST_ABILITIES = new ResourceLocation("miztinker", "extra_abilities");
    private static final ResourceLocation PERSIST_DEFENSE = new ResourceLocation("miztinker", "extra_defense");
    private static final ResourceLocation PERSIST_SOUL = new ResourceLocation("miztinker", "extra_souls");

    private static final ResourceLocation EVOLVED_FLAG = new ResourceLocation("miztinker", "evolution_triggered");
    private static final ResourceLocation MESSAGE_SENT = new ResourceLocation("miztinker", "evolution_message_sent");

    private static final ResourceLocation FX_TRIGGERED = new ResourceLocation("miztinker", "evolution_fx_triggered");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.VOLATILE_DATA);
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }


    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT nbt) {
        ModDataNBT persistentData = (ModDataNBT) context.getPersistentData();
        checkAndAssign(persistentData);

        if (persistentData.contains(PERSIST_UPGRADES)) nbt.addSlots(SlotType.UPGRADE, persistentData.getInt(PERSIST_UPGRADES));
        if (persistentData.contains(PERSIST_ABILITIES)) nbt.addSlots(SlotType.ABILITY, persistentData.getInt(PERSIST_ABILITIES));
        if (persistentData.contains(PERSIST_DEFENSE)) nbt.addSlots(SlotType.DEFENSE, persistentData.getInt(PERSIST_DEFENSE));
        if (persistentData.contains(PERSIST_SOUL)) nbt.addSlots(SlotType.SOUL, persistentData.getInt(PERSIST_SOUL));
    }

    private void checkAndAssign(ModDataNBT data) {
        if (data.getBoolean(EVOLVED_FLAG)) return;

        int level = data.getInt(ImprovableModifier.LEVEL_KEY);
        if (level >= Config.maxLevel.get()) {
            Random rand = new Random();
            int u = 0, a = 0, d = 0, s = 0;
            for (int i = 0; i < 10; i++) {
                int r = rand.nextInt(4);
                if (r == 0) u++; else if (r == 1) a++; else if (r == 2) d++; else s++;
            }
            data.putInt(PERSIST_UPGRADES, u);
            data.putInt(PERSIST_ABILITIES, a);
            data.putInt(PERSIST_DEFENSE, d);
            data.putInt(PERSIST_SOUL, s);

            data.putBoolean(EVOLVED_FLAG, true);
        }
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (!world.isClientSide && holder instanceof Player player) {
            ModDataNBT data = tool.getPersistentData();

            if (data.getBoolean(EVOLVED_FLAG) && !data.getBoolean(FX_TRIGGERED)) {

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.0F);
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 2.0F, 0.5F);

                if (world instanceof ServerLevel serverWorld) {
                    serverWorld.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            3, 0.2, 0.2, 0.2, 0.0);

                    serverWorld.sendParticles(ParticleTypes.ENCHANT,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            100, 0.5, 1.0, 0.5, 0.2);

                    serverWorld.sendParticles(ParticleTypes.FLASH,
                            player.getX(), player.getY() + 1.5, player.getZ(),
                            10, 0.1, 0.1, 0.1, 0.0);
                }

                if (isSelected && !data.getBoolean(MESSAGE_SENT)) {
                    player.displayClientMessage(Component.translatable("message.miztinker.ultimate_evolution.success_10"), true);
                    data.putBoolean(MESSAGE_SENT, true);
                }

                data.putBoolean(FX_TRIGGERED, true);
            }
        }
    }
}
