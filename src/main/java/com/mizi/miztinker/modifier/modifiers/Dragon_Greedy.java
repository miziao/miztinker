package com.mizi.miztinker.modifier.modifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dragon_Greedy extends NoLevelsModifier implements ProcessLootModifierHook {

    private static final Random RANDOM = new Random();

    // 冰火传说稀有生物列表
    private static final List<ResourceLocation> ICE_AND_FIRE_MOBS = new ArrayList<>();

    static {
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("iceandfire", "hippocampus"));
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("iceandfire", "hippogryph"));
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("iceandfire", "cockatrice"));
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("iceandfire", "siren"));
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("iceandfire", "pixie"));
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("iceandfire", "deathworm"));
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("iceandfire", "hydra"));
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("iceandfire", "sea_serpent"));
        ICE_AND_FIRE_MOBS.add(new ResourceLocation("goety", "wraith"));
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public void processLoot(IToolStackView iToolStackView, ModifierEntry modifierEntry, List<ItemStack> list, LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity == null) return;

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null) return;

        // 判断是否为冰火生物
        if (ICE_AND_FIRE_MOBS.contains(id)) {
            ItemStack rareDrop = getRareDrop(id);
            if (!rareDrop.isEmpty()) {
                list.add(rareDrop.copy());
            }
        } else {
            // 非冰火生物 -> 双倍掉落
            List<ItemStack> duplicates = new ArrayList<>();
            for (ItemStack stack : list) {
                if (!stack.isEmpty()) {
                    duplicates.add(stack.copy());
                }
            }
            list.addAll(duplicates);
        }
    }

    // 返回指定生物的稀有掉落物
    private ItemStack getRareDrop(ResourceLocation id) {
        Item item = null;
        switch (id.getPath()) {
            case "hippocampus" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "hippocampus_fin"));
            case "hippogryph" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "hippogryph_talon"));
            case "cockatrice" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "cockatrice_eye"));
            case "siren" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "siren_tear"));
            case "pixie" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "pixie_wings"));
            case "deathworm" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "deathworm_tongue"));
            case "hydra" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "hydra_heart"));
            case "sea_serpent" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "sea_serpent_fang"));
            case "wraith" -> item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("iceandfire", "ghost_ingot"));
        }
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    @Override
    public int getPriority() {
        return 80; // 优先级高于大部分常规掉落处理
    }

}