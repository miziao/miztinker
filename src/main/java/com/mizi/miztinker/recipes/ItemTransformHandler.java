package com.mizi.miztinker.recipes;

import com.mizi.miztinker.recipes.rules.*;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = "miztinker", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemTransformHandler {

    private static final String TAG_TRANSFORMABLE = "miztinker:transformable";
    private static final String TAG_ORIGIN_Y = "miztinker:origin_y";

    private static final List<ITransformRule> RULES = Arrays.asList(
            new StarMetalRule(),
            new DeathNoteRule(),
            new StormBookRule()
    );

    private static final Map<ProcessKey, ActiveProcess> activeProcesses = new HashMap<>();

    private record ProcessKey(ResourceKey<Level> dimension, UUID entityUuid) {}

    private record ActiveProcess(ITransformRule rule, long startTick) {}

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!(event.getEntity() instanceof ItemEntity item)) {
            return;
        }

        ItemStack stack = item.getItem();

        for (ITransformRule rule : RULES) {
            if (rule.isInput(stack)) {
                item.getPersistentData().putBoolean(TAG_TRANSFORMABLE, true);

                if (stack.is(Items.WRITABLE_BOOK)
                        && !item.getPersistentData().contains(TAG_ORIGIN_Y, Tag.TAG_DOUBLE)) {
                    item.getPersistentData().putDouble(TAG_ORIGIN_Y, item.getY());
                }

                break;
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel level)) {
            return;
        }

        scanNewProcesses(level);
        updateActiveProcesses(level);
    }

    private static void scanNewProcesses(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof ItemEntity item)) {
                continue;
            }

            if (item.isRemoved() || item.getItem().isEmpty()) {
                continue;
            }

            ItemStack stack = item.getItem();

            boolean transformable = item.getPersistentData().getBoolean(TAG_TRANSFORMABLE);

            if (!transformable) {
                for (ITransformRule rule : RULES) {
                    if (rule.isInput(stack)) {
                        item.getPersistentData().putBoolean(TAG_TRANSFORMABLE, true);
                        transformable = true;
                        break;
                    }
                }
            }

            if (!transformable) {
                continue;
            }

            ProcessKey key = keyOf(level, item);

            if (activeProcesses.containsKey(key)) {
                continue;
            }

            for (ITransformRule rule : RULES) {
                if (rule.isInput(stack) && rule.matches(item, level)) {
                    handleRuleTrigger(item, level, rule);
                    break;
                }
            }
        }
    }

    private static void handleRuleTrigger(ItemEntity item, ServerLevel level, ITransformRule rule) {
        if (rule.getTransformTicks() <= 0) {
            performTransform(item, level, rule);
            return;
        }

        activeProcesses.putIfAbsent(
                keyOf(level, item),
                new ActiveProcess(rule, level.getGameTime())
        );
    }

    private static void updateActiveProcesses(ServerLevel level) {
        Iterator<Map.Entry<ProcessKey, ActiveProcess>> it = activeProcesses.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<ProcessKey, ActiveProcess> entry = it.next();
            ProcessKey key = entry.getKey();

            if (!key.dimension.equals(level.dimension())) {
                continue;
            }

            ItemEntity item = findItemByUuid(level, key.entityUuid);

            if (item == null || item.isRemoved() || item.getItem().isEmpty()) {
                it.remove();
                continue;
            }

            ActiveProcess process = entry.getValue();

            if (!process.rule.isInput(item.getItem())) {
                it.remove();
                continue;
            }

            if (!process.rule.matches(item, level)) {
                it.remove();
                continue;
            }

            int elapsedTicks = (int) (level.getGameTime() - process.startTick);
            int requiredTicks = process.rule.getTransformTicks();

            item.setPickUpDelay(20);
            process.rule.onTransforming(item, level, elapsedTicks, requiredTicks);

            if (elapsedTicks >= requiredTicks) {
                performTransform(item, level, process.rule);
                it.remove();
            }
        }
    }

    private static void performTransform(ItemEntity oldEntity, ServerLevel level, ITransformRule rule) {
        ItemStack input = oldEntity.getItem();

        if (input.isEmpty()) {
            return;
        }

        ItemStack result = rule.getResult(input.copy(), level);

        if (result.isEmpty()) {
            return;
        }

        // 关键：整组转化
        result.setCount(input.getCount());

        ItemEntity newEntity = new ItemEntity(
                level,
                oldEntity.getX(),
                oldEntity.getY(),
                oldEntity.getZ(),
                result
        );

        newEntity.setDeltaMovement(oldEntity.getDeltaMovement());
        newEntity.setPickUpDelay(20);

        level.addFreshEntity(newEntity);

        rule.onTransformed(newEntity, level, result);

        oldEntity.discard();
    }

    private static ProcessKey keyOf(ServerLevel level, ItemEntity item) {
        return new ProcessKey(level.dimension(), item.getUUID());
    }

    private static ItemEntity findItemByUuid(ServerLevel level, UUID uuid) {
        Entity entity = level.getEntity(uuid);

        if (entity instanceof ItemEntity item) {
            return item;
        }

        return null;
    }
}