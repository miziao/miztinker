package com.mizi.miztinker.util;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AppendLootModifier extends LootModifier {
    public static final Supplier<Codec<AppendLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create((inst) -> codecStart(inst).and(
                    Codec.STRING.fieldOf("key").forGetter((m) -> m.resourceLocationKey)
            ).apply(inst, AppendLootModifier::new))
    );

    private final String resourceLocationKey;

    public AppendLootModifier(LootItemCondition[] conditionsIn, String resourceLocationKey) {
        super(conditionsIn);
        this.resourceLocationKey = resourceLocationKey;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation path = new ResourceLocation(this.resourceLocationKey);

        LootTable lootTable = context.getLevel().getServer().getLootData().getLootTable(path);

        lootTable.getRandomItemsRaw(context, generatedLoot::add);

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}