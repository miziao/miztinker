package com.mizi.miztinker.modifier.modifiers.base;

import net.minecraft.server.level.ServerLevel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DelayedTaskHandler {

    private static final List<DelayedTask> TASKS = new LinkedList<>();

    public static void add(ServerLevel level, int delayTicks, Runnable action) {
        TASKS.add(new DelayedTask(level, delayTicks, action));
    }

    public static void tick(ServerLevel level) {
        Iterator<DelayedTask> it = TASKS.iterator();
        while (it.hasNext()) {
            DelayedTask task = it.next();
            if (task.level != level) continue;

            task.ticks--; // ✅ 现在可以修改了
            if (task.ticks <= 0) {
                task.action.run();
                it.remove();
            }
        }
    }


    private static class DelayedTask {
        final ServerLevel level;
        int ticks;
        final Runnable action;

        private DelayedTask(ServerLevel level, int ticks, Runnable action) {
            this.level = level;
            this.ticks = ticks;
            this.action = action;
        }
    }
}