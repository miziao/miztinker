package com.mizi.miztinker.modifier.modifiers;

import com.mizi.miztinker.modifier.register.MiztinkerItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockHarvestModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Random;

public class Refill extends Modifier implements
        MeleeHitModifierHook,
        OnAttackedModifierHook,
        BlockHarvestModifierHook,
        ProcessLootModifierHook {

    private static final Random RANDOM = new Random();

    private static float getDropChance(int level) {
        return Math.min(0.1f * (1 << (level - 1)), 1.0f);
    }

    @Override
    protected void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
        hookBuilder.addHook(this, ModifierHooks.ON_ATTACKED);
        hookBuilder.addHook(this, ModifierHooks.BLOCK_HARVEST);
        hookBuilder.addHook(this, ModifierHooks.PROCESS_LOOT); // 关键
    }

    /** 攻击生物掉落 */
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        Player player = context.getPlayerAttacker();
        if (target == null || player == null) return;

        // 只在目标死掉后触发掉落
        if (!target.isAlive()) {
            dropCap(target.level(), target.getX(), target.getY(), target.getZ(), getDropChance(modifier.getLevel()));
        }
    }

    /** 被攻击时掉落 */
    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context,
                           net.minecraft.world.entity.EquipmentSlot slot,
                           net.minecraft.world.damagesource.DamageSource source, float amount, boolean isDirectDamage) {
        if (!(context.getEntity() instanceof Player player)) return;
        dropCap(player.level(), player.getX(), player.getY(), player.getZ(), getDropChance(modifier.getLevel()));
    }

    /** 挖掘方块掉落 */
    @Override
    public void finishHarvest(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context, int level) {
        Player player = context.getPlayer();
        if (player == null) return;

        dropCap(player.level(),
                context.getPos().getX() + 0.5,
                context.getPos().getY() + 0.5,
                context.getPos().getZ() + 0.5,
                getDropChance(modifier.getLevel()));
    }

    /** loot 阶段掉落，确保生物生成掉落 */
    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot, LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof LivingEntity living && !living.isAlive()) {
            if (RANDOM.nextFloat() <= getDropChance(modifier.getLevel())) {
                generatedLoot.add(new ItemStack(MiztinkerItems.MOZHUA_CAP.get()));
            }
        }
    }

    /** 工具生成掉落物 */
    private void dropCap(Level world, double x, double y, double z, float chance) {
        if (!world.isClientSide && RANDOM.nextFloat() <= chance) {
            world.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(world, x, y, z, new ItemStack(MiztinkerItems.MOZHUA_CAP.get())));
        }
    }
}