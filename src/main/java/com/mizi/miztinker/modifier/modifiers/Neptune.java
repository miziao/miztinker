package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;

import java.util.ArrayList;
import java.util.List;

public class Neptune extends NoLevelsModifier implements MeleeHitModifierHook {

    private static final ModifierId LUCK_MODIFIER_ID = new ModifierId("tconstruct", "luck");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!(context.getAttacker().level() instanceof ServerLevel world)) return;

        Entity attacker = context.getAttacker();
        Entity target = context.getTarget();

        boolean isAttackerInWater = hasWaterVertical(world, attacker.blockPosition());
        boolean isTargetInWater = hasWaterVertical(world, target.blockPosition());

        boolean canAccessTreasure = isAttackerInWater || isTargetInWater;

        ItemStack fakeRod = new ItemStack(Items.FISHING_ROD);
        int luckLevel = tool.getModifierLevel(LUCK_MODIFIER_ID);
        if (luckLevel > 0) {
            fakeRod.enchant(Enchantments.FISHING_LUCK, luckLevel);
        }

        float totalLuck = (attacker instanceof Player p ? p.getLuck() : 0) + luckLevel;

        LootParams lootparams = new LootParams.Builder(world)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.TOOL, fakeRod)
                .withParameter(LootContextParams.THIS_ENTITY, attacker)
                .withLuck(totalLuck)
                .create(LootContextParamSets.FISHING);

        List<ItemStack> finalDrops = new ArrayList<>();

        if (canAccessTreasure) {
            float treasureChance = 0.2f + (luckLevel * 0.1f);
            if (world.random.nextFloat() < treasureChance) {
                LootTable treasureTable = world.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING_TREASURE);
                finalDrops.addAll(treasureTable.getRandomItems(lootparams));
            }
        }

        if (finalDrops.isEmpty()) {
            LootTable loottable = world.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
            finalDrops.addAll(loottable.getRandomItems(lootparams));
        }

        if (finalDrops.isEmpty()) {
            finalDrops.addAll(world.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING_FISH).getRandomItems(lootparams));
        }

        for (ItemStack drop : finalDrops) {
            if (!drop.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world, target.getX(), target.getY() + 0.5D, target.getZ(), drop.copy());
                itemEntity.setDeltaMovement(world.random.nextGaussian() * 0.05D, 0.3D, world.random.nextGaussian() * 0.05D);
                world.addFreshEntity(itemEntity);
            }
        }
    }

    private boolean hasWaterVertical(ServerLevel world, BlockPos pos) {
        for (int i = 0; i <= 3; i++) {
            if (world.getFluidState(pos.above(i)).is(Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }
}