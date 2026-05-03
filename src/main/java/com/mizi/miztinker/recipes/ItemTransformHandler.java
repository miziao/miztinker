package com.mizi.miztinker.recipes;

import com.mizi.miztinker.recipes.rules.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "miztinker")
public class ItemTransformHandler {

    private static final String TAG_TRANSFORMABLE = "miztinker:transformable";

    private static final List<ITransformRule> RULES = Arrays.asList(
            new StarMetalRule(),
            new DeathNoteRule(),
            new StormBookRule()
    );

    private static final Map<Integer, ActiveProcess> activeProcesses = new ConcurrentHashMap<>();

    record ActiveProcess(ITransformRule rule, long startTick) {}

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;

        if (event.getEntity() instanceof ItemEntity item) {
            ItemStack stack = item.getItem();
            for (ITransformRule rule : RULES) {
                if (rule.isInput(stack)) {

                    item.getPersistentData().putBoolean(TAG_TRANSFORMABLE, true);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.level instanceof ServerLevel level) || level.getGameTime() % 20 != 0) return;

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof ItemEntity item && item.getPersistentData().getBoolean(TAG_TRANSFORMABLE)) {
                for (ITransformRule rule : RULES) {
                    if (rule.isInput(item.getItem()) && rule.matches(item, level)) {
                        handleRuleTrigger(item, level, rule);
                        break;
                    }
                }
            }
        }

        updateActiveProcesses(level);
    }

    private static void handleRuleTrigger(ItemEntity item, ServerLevel level, ITransformRule rule) {
        if (rule.getTransformTicks() <= 0) {
            performTransform(item, level, rule.getResult(item.getItem(), level));
        } else {
            activeProcesses.putIfAbsent(item.getId(), new ActiveProcess(rule, level.getGameTime()));
        }
    }

    private static void updateActiveProcesses(ServerLevel level) {
        Iterator<Map.Entry<Integer, ActiveProcess>> it = activeProcesses.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ActiveProcess> entry = it.next();
            Entity rawEntity = level.getEntity(entry.getKey());

            if (!(rawEntity instanceof ItemEntity entity) || entity.isRemoved()) {
                it.remove();
                continue;
            }

            ActiveProcess process = entry.getValue();


            if (!process.rule.matches(entity, level)) {
                it.remove();
                continue;
            }

            if (level.getGameTime() - process.startTick >= process.rule.getTransformTicks()) {
                performTransform(entity, level, process.rule.getResult(entity.getItem(), level));
                it.remove();
            }
        }
    }

    private static void performTransform(ItemEntity oldEntity, ServerLevel level, ItemStack result) {
        if (result.isEmpty()) return;

        ItemEntity newEntity = new ItemEntity(level, oldEntity.getX(), oldEntity.getY(), oldEntity.getZ(), result.copy());
        newEntity.setDeltaMovement(oldEntity.getDeltaMovement());

        level.addFreshEntity(newEntity);


        oldEntity.discard();
    }
}