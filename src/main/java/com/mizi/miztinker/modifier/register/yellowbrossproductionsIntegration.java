package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.miztinker;
import com.mizi.miztinker.modifier.modifiers.AwakenDoomGuy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class yellowbrossproductionsIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(yellowbrossproductionsIntegration.class);

    public static void registerModifiers() {
        try {
            MiztinkerModifiers.MODIFIERS.register("awaken_doomguy", AwakenDoomGuy::new);
            LOGGER.info("Successfully registered some modifier with yellowbrossproductionsIntegration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

