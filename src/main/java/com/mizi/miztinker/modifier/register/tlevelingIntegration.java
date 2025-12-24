package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.modifier.modifiers.LearningDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class tlevelingIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(tlevelingIntegration.class);
    public static void registerModifiers(){
        try {
            MiztinkerModifiers.MODIFIERS.register("learningdevice", LearningDevice::new);
            LOGGER.info("Successfully registered some modifier with tleveling integration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

