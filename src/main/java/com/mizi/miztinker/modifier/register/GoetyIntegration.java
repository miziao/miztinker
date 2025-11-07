package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.modifiers.VexSummoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class GoetyIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoetyIntegration.class);

    public static void registerModifiers() {
        try {
            MiztinkerModifiers.MODIFIERS.register("vexsummoner", VexSummoner::new);
            LOGGER.info("Successfully registered some modifier with GoetyIntegration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

