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
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!(context.getAttacker().level() instanceof ServerLevel world) || damageDealt <= 0) return;

        Entity attacker = context.getAttacker();
        Entity target = context.getTarget();
        BlockPos pos = target.blockPosition();

        boolean isWetEnvironment = hasWaterVertical(world, attacker.blockPosition())
                || hasWaterVertical(world, pos)
                || world.isRainingAt(pos);

        ItemStack fakeRod = new ItemStack(Items.FISHING_ROD);
        int luckLevel = tool.getModifierLevel(LUCK_MODIFIER_ID);

        if (luckLevel > 0) {
            fakeRod.enchant(Enchantments.FISHING_LUCK, luckLevel);
        }

        float playerLuck = (attacker instanceof Player p) ? p.getLuck() : 0.0f;
        float totalLuck = playerLuck + luckLevel;

        LootParams lootparams = new LootParams.Builder(world)
                .withParameter(LootContextParams.ORIGIN, target.position())
                .withParameter(LootContextParams.TOOL, fakeRod)
                .withParameter(LootContextParams.THIS_ENTITY, target)
                .withParameter(LootContextParams.KILLER_ENTITY, attacker)
                .withLuck(totalLuck)
                .create(LootContextParamSets.FISHING);

        List<ItemStack> drops = new ArrayList<>();

        if (isWetEnvironment) {
            float treasureChance = 0.1f + (luckLevel * 0.05f);
            if (world.random.nextFloat() < treasureChance) {
                LootTable treasureTable = world.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING_TREASURE);
                drops.addAll(treasureTable.getRandomItems(lootparams));
            }
        }

        if (drops.isEmpty()) {
            LootTable mainFishingTable = world.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
            drops.addAll(mainFishingTable.getRandomItems(lootparams));
        }

        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world,
                        target.getX(), target.getY() + 0.5D, target.getZ(),
                        stack.copy()
                );
                itemEntity.setDeltaMovement(
                        world.random.nextGaussian() * 0.02D,
                        0.3D,
                        world.random.nextGaussian() * 0.02D
                );
                world.addFreshEntity(itemEntity);
            }
        }
    }

    private boolean hasWaterVertical(ServerLevel world, BlockPos pos) {
        for (int i = -1; i <= 2; i++) {
            if (world.getFluidState(pos.above(i)).is(Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }
}