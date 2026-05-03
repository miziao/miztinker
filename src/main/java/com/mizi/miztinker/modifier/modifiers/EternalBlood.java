package com.mizi.miztinker.modifier.modifiers;

import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class EternalBlood extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry entry, Level world,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, net.minecraft.world.item.ItemStack stack) {
        if (!isCorrectSlot) return;
        if (!(holder instanceof Player player)) return;

        VampirePlayer vampire = VampirePlayer.get(player);
        if (vampire.getLevel() <= 0) return;

        fixTinkersTankNBT(player);

        try {
            vampire.drinkBlood(1, 1, null);
        } catch (Exception ignored) {
        }
    }

    private void fixTinkersTankNBT(Player player) {
        for (net.minecraft.world.item.ItemStack inv : player.getInventory().items) {
            if (inv.getItem().getClass().getName().contains("TankItem")) {

                CompoundTag tag = inv.getOrCreateTag();

                if (!tag.contains("tank")) {
                    CompoundTag tank = new CompoundTag();
                    tank.putString("FluidName", "minecraft:empty");
                    tank.putInt("Amount", 0);
                    tag.put("tank", tank);
                }
            }
        }
    }
}