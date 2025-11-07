package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.modifiers.Wall_of_Skeleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ironsspellbooksIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ironsspellbooksIntegration.class);

    public static void registerModifiers() {
        try {
            MiztinkerModifiers.MODIFIERS.register("wall_of_skeleton", Wall_of_Skeleton::new);
            LOGGER.info("Successfully registered some modifier with ironsspellbooksIntegration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

