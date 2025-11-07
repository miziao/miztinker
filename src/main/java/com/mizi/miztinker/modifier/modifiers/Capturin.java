package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;

public class Capturin extends Modifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry entry,
                              ToolAttackContext context, float damageDealt) {
        Player player = context.getPlayerAttacker();
        LivingEntity target = context.getLivingTarget();
        Level world = context.getLevel();

        if (world.isClientSide || player == null || target == null || !target.isDeadOrDying())
            return;

        // 每级增加 2.5% 概率
        float chance = entry.getLevel() * 0.025f;

        if (world.random.nextFloat() < chance) {
            // 获取目标实体对应的刷怪蛋
            Item eggItem = ForgeSpawnEggItem.fromEntityType(target.getType());
            if (eggItem != null) {
                ItemStack egg = new ItemStack(eggItem);

                // 在目标死亡位置生成掉落物
                ItemEntity drop = new ItemEntity(world,
                        target.getX(), target.getY(), target.getZ(),
                        egg);
                world.addFreshEntity(drop);
            }
        }
    }

    @Override
    public int getPriority() {
        return 75; // 适中优先级
    }
}
