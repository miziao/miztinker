package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.modifier.modifiers.Atomslash;
import com.mizi.miztinker.modifier.modifiers.CircleSlash;
import com.mizi.miztinker.modifier.modifiers.Gatling_Sword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class SlashBladeIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlashBladeIntegration.class);

    public static void registerModifiers() {
        try {
            MiztinkerModifiers.MODIFIERS.register("atomslash", Atomslash::new);
            MiztinkerModifiers.MODIFIERS.register("circleslash", CircleSlash::new);
            MiztinkerModifiers.MODIFIERS.register("gatling_sword", Gatling_Sword::new);
            LOGGER.info("Successfully registered some modifier with Etstlib_SlashBladeIntegration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

