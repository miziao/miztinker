package com.mizi.miztinker.modifier.modifiers;


import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

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

        // 获取吸血鬼数据
        VampirePlayer vampire = VampirePlayer.get(player);
        if (vampire.getLevel() <= 0) return; // 不是吸血鬼，不处理

        // 持续喝血
        vampire.drinkBlood(1, 1, null);
    }
}

    
