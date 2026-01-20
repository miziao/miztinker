package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.modifier.modifiers.Stredgeuniverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Wzz_SlashBladeIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Wzz_SlashBladeIntegration.class);

    public static void registerModifiers() {
        try {
            MiztinkerModifiers.MODIFIERS.register("stredgeuniverse", Stredgeuniverse::new);
            LOGGER.info("Successfully registered some modifier with Wzz_Etstlib_SlashBladeIntegration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

