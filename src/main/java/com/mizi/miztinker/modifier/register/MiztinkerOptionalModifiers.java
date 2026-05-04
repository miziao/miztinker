package com.mizi.miztinker.modifier.register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mizi.miztinker.item.tool.until.OptionalDependencyHandler;

public class MiztinkerOptionalModifiers {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiztinkerOptionalModifiers.class);

    public static void voidregisterOptionalModifiers() {

        if (OptionalDependencyHandler.isClassPresent("mods.flammpfeil.slashblade.SlashBlade")) {
            try {
                SlashBladeIntegration.registerModifiers();
                LOGGER.info("Successfully initialized  SlashBlade integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize  SlashBlade integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info(" SlashBlade not found, some modifier will not be registered");
        }


        if (OptionalDependencyHandler.isClassPresent("virtuoel.pehkui.api.ScaleData")) {
            try {
                virtuoelIntegration.registerModifiers();
                LOGGER.info("Successfully initialized virtuoel integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize virtuoel integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("virtuoel not found, some modifier will not be registered");

        }

        if (OptionalDependencyHandler.isClassPresent("de.teamlapen.vampirism.entity.player.vampire.VampirePlayer")) {
            try {
                VampireIntegration.registerModifiers();
                LOGGER.info("Successfully initialized Vampire integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Vampire integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("Vampire not found, some modifier will not be registered");

        }

        if (OptionalDependencyHandler.isClassPresent("mods.flammpfeil.slashblade.SlashBlade") &&
                OptionalDependencyHandler.isClassPresent("net.wzz.stredgeuniverse.entity.MeteoriteSwordEntity")) {
            try {
                Wzz_SlashBladeIntegration.registerModifiers();
                LOGGER.info("Successfully initialized Wzz and  SlashBlade integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Wzz and  SlashBlade integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("Wzz and  SlashBlade not found, some modifier will not be registered");
        }

        if (OptionalDependencyHandler.isClassPresent("com.Polarice3.Goety.common.entities.ModEntityType")) {
            try {
                GoetyIntegration.registerModifiers();
                LOGGER.info("Successfully initialized Goety integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Goety integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("Goety not found, some modifier will not be registered");

        }

        if (OptionalDependencyHandler.isClassPresent("io.redspace.ironsspellbooks.api.registry.AttributeRegistry")) {
            try {
                ironsspellbooksIntegration.registerModifiers();
                LOGGER.info("Successfully initialized ironsspellbooks integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize ironsspellbooks integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("ironsspellbooks not found, some modifier will not be registered");

        }


    }
}
