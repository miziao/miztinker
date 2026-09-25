package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;

public class SandBindingFuneral extends NoLevelsModifier implements MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target == null || context.getLevel().isClientSide) return;

        int currentDurability = tool.getCurrentDurability();
        if (currentDurability <= 0) return;

        net.minecraft.world.item.ItemStack stack = context.getAttacker().getItemInHand(context.getHand());

        ToolDamageUtil.damage(tool, currentDurability, context.getAttacker(), stack);

        float extraDamage = currentDurability * 100.0f;
        target.hurt(target.damageSources().dryOut(), extraDamage);

        spawnSandFuneral(context.getLevel(), target);
    }


    private void spawnSandFuneral(Level level, LivingEntity target) {
        BlockPos basePos = target.blockPosition().above(2);
        int size = 2;
        int height = 5;

        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                for (int y = 0; y < height; y++) {
                    BlockPos spawnPos = basePos.offset(x, y, z);

                    if (level.isEmptyBlock(spawnPos)) {
                        FallingBlockEntity sand = FallingBlockEntity.fall(
                                level,
                                spawnPos,
                                Blocks.SAND.defaultBlockState()
                        );

                        sand.time = 1;

                        level.addFreshEntity(sand);
                    }
                }
            }
        }
    }
}