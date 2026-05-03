package com.mizi.miztinker.util;

public class Time {
    private static boolean isTimeStop = false;
    public static float pausePartialTick = 0f;

    public static boolean get() {
        return isTimeStop;
    }

    public static void set(boolean stopped) {
        isTimeStop = stopped;
    }
}