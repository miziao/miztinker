package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.registries.BuiltInRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class ScorchedBone extends NoLevelsModifier implements ProcessLootModifierHook {

    private static final ResourceLocation NECROTIC_BONE_ID = ResourceLocation.fromNamespaceAndPath("tconstruct", "necrotic_bone");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot, LootContext context) {
        // 没有伤害来源说明不是击杀触发
        if (!context.hasParam(LootContextParams.DAMAGE_SOURCE)) {
            return;
        }

        // 获取被击杀的实体
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof AbstractSkeleton) || entity instanceof WitherSkeleton) {
            return;
        }

        // === 50% 概率掉落凋零之骨 ===
        if (RANDOM.nextFloat() < 0.5f) {
            Item necroticBone = BuiltInRegistries.ITEM.getOptional(NECROTIC_BONE_ID).orElse(Items.BONE);
            generatedLoot.add(new ItemStack(necroticBone));
        }

        // === 100% 掉落凋零骷髅头 ===
        boolean hasHead = generatedLoot.stream().anyMatch(stack -> stack.is(Items.WITHER_SKELETON_SKULL));
        if (!hasHead) {
            generatedLoot.add(new ItemStack(Items.WITHER_SKELETON_SKULL));
        }
    }
}