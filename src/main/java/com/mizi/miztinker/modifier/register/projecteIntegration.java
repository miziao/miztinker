package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.modifier.modifiers.EMC_torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class projecteIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(projecteIntegration.class);

    public static void registerModifiers() {
        try {
            MiztinkerModifiers.MODIFIERS.register("emc_torrent", EMC_torrent::new);
            LOGGER.info("Successfully registered some modifier with projecteIntegration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

