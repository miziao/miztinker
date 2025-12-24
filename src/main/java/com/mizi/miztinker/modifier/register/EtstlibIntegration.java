package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.modifier.modifiers.LoveStone;
import com.mizi.miztinker.modifier.modifiers.BloodMary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtstlibIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtstlibIntegration.class);
    public static void registerModifiers(){
        try {
            MiztinkerModifiers.MODIFIERS.register("bloodmary", BloodMary::new);
            MiztinkerModifiers.MODIFIERS.register("lovestone", LoveStone::new);
            LOGGER.info("Successfully registered some modifier with etstlib integration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}
