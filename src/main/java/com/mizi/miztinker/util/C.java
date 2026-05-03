package com.mizi.miztinker.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class C {
    private static final ChatFormatting[] color = {
            ChatFormatting.DARK_BLUE,
            ChatFormatting.AQUA,
            ChatFormatting.BLUE,
            ChatFormatting.DARK_AQUA,
            ChatFormatting.DARK_GREEN,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.GOLD,
            ChatFormatting.DARK_RED,
            ChatFormatting.YELLOW,
            ChatFormatting.RED,
            ChatFormatting.GREEN,
            ChatFormatting.LIGHT_PURPLE
    };

    public static Component getRainbowComponent(String input) {
        return formattingToComponent(input, color, 80.0D);
    }

    public static Component formattingToComponent(String input, ChatFormatting[] colours, double delay) {
        MutableComponent root = Component.literal("");

        if (delay <= 0.2D) delay = 0.9999D;

        int offset = (int) Math.floor((System.currentTimeMillis() & 0x3FFFL) / delay) % colours.length;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            ChatFormatting currentColor = colours[(colours.length + i - offset) % colours.length];
            root.append(Component.literal(String.valueOf(c)).withStyle(currentColor));
        }
        return root;
    }
}