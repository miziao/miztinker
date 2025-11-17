package com.mizi.miztinker.modifier.register;


import com.mizi.miztinker.modifier.modifiers.AwakenUltraman;
import com.mizi.miztinker.modifier.modifiers.GreyMatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class virtuoelIntegration {
    private static final Logger LOGGER = LoggerFactory.getLogger(virtuoelIntegration.class);

    public static void registerModifiers(){
        try {
            MiztinkerModifiers.MODIFIERS.register("awaken_ultraman", AwakenUltraman::new);
            MiztinkerModifiers.MODIFIERS.register("greymatter", GreyMatter::new);
            LOGGER.info("Successfully registered some modifier with virtuoelIntegration");
        } catch (Throwable t) {
            LOGGER.error("Failed to register some modifier: {}", t.getMessage());
            t.printStackTrace();
        }
    }
}

