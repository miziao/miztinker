package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerModifiers;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.DamageBlockModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodShield extends NoLevelsModifier implements DamageBlockModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.DAMAGE_BLOCK);
    }

    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry entry, EquipmentContext context,
                                   EquipmentSlot slot, DamageSource source, float damage) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        VampirePlayer vampire = VampirePlayer.get(player);
        if (vampire.getLevel() <= 0) return false;

        if (damage <= 30) return true;

        int currentBlood = vampire.getBloodLevel();
        int bloodCost = (int) Math.ceil(damage / 200.0);

        if (currentBlood >= bloodCost) {
            if (vampire.useBlood(bloodCost, false)) {
                if (bloodCost > 1) {
                    player.sendSystemMessage(Component.translatable("modifier.miztinker.blood_shield.consume", bloodCost));
                }
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (hasBloodShieldArmor(player)) {
                VampirePlayer vampire = VampirePlayer.get(player);
                DamageSource source = event.getSource();

                if (vampire.getBloodLevel() > 0 && !Helper.canKillVampires(source)) {

                    event.setCanceled(true);

                    player.setHealth(0.5f);

                    player.sendSystemMessage(Component.translatable("modifier.miztinker.blood_shield.immortal"));
                }
            }
        }
    }

    public static boolean hasBloodShieldArmor(LivingEntity entity) {
        if (entity == null) return false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    try {
                        IToolStackView tool = ToolStack.from(stack);
                        if (tool.getModifierLevel(MiztinkerModifiers.BLOODSHIDID.get()) > 0) {
                            return true;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return false;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}