package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.modifiers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class VampireIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(VampireIntegration.class);

    public static void registerModifiers() {
        try {
            MiztinkerModifiers.MODIFIERS.register("blood_bat", Blood_bat::new);
            MiztinkerModifiers.MODIFIERS.register("draculasblood", DraculasBlood::new);
            MiztinkerModifiers.MODIFIERS.register("eternalblood", EternalBlood::new);
            MiztinkerModifiers.MODIFIERS.register("blood_wing", Blood_Wing::new);
            MiztinkerModifiers.MODIFIERS.register("bloodshield", BloodShield::new);
            MiztinkerModifiers.MODIFIERS.register("eternalsunscreen", EternalSunscreen::new);
            LOGGER.info("Successfully registered some modifier with VampireIntegration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

