package com.mizi.miztinker.modifier.modifiers;

import de.teamlapen.vampirism.entity.player.hunter.HunterPlayer;
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

import java.util.Optional;

public class LungsPast extends NoLevelsModifier implements InventoryTickModifierHook {

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level,
                                LivingEntity holder, int itemSlot, boolean isSelected,
                                boolean isCorrectSlot, ItemStack stack) {
        // 必须在服务端运行，且物品在正确的槽位
        if (level.isClientSide || !isCorrectSlot) return;
        if (!(holder instanceof Player player)) return;

        // 检查是否是猎人，并且猎人等级 >= 1
        Optional<HunterPlayer> optHunter = HunterPlayer.getOpt(player).resolve();
        if (optHunter.isEmpty() || optHunter.get().getLevel() <= 0) return;

        // 读取空气值
        int currentAir = player.getAirSupply();
        int maxAir = player.getMaxAirSupply();

        // 上一次空气值（用于检测正在回氧）
        int lastAir = player.getPersistentData().getInt("miztinker_last_air");

        // 若氧气正在回升（即开始呼吸），则直接瞬间回满
        if (currentAir > lastAir && currentAir < maxAir) {
            player.setAirSupply(maxAir);
        }

        // 记录当前空气值
        player.getPersistentData().putInt("miztinker_last_air", currentAir);
    }

    /** 注册钩子 */
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
}