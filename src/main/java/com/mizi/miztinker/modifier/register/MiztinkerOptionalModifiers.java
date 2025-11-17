package com.mizi.miztinker.modifier.register;

import com.mizi.miztinker.item.tool.until.OptionalDependencyHandler;

import static com.mojang.text2speech.Narrator.LOGGER;
public class MiztinkerOptionalModifiers  {

    public static void voidregisterOptionalModifiers() {
        // 检测etstlib是否存在
        if (OptionalDependencyHandler.isClassPresent("com.c2h6s.etstlib.tool.modifiers.base.EtSTBaseModifier")) {
            try {
                EtstlibIntegration.registerModifiers();
                LOGGER.info("Successfully initialized etstlib integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize etstlib integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("etstlib not found, some modifier will not be registered");

        }

        if (OptionalDependencyHandler.isClassPresent("mods.flammpfeil.slashblade.SlashBlade") &&
                OptionalDependencyHandler.isClassPresent("com.c2h6s.etstlib.register.EtSTLibHooks")) {
            try {
                Etstlib_SlashBladeIntegration.registerModifiers();
                LOGGER.info("Successfully initialized Etstlib and SlashBlade integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Etstlib and SlashBlade integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("etstlib and SlashBlade not found, some modifier will not be registered");
        }

        if (OptionalDependencyHandler.isClassPresent("com.yellowbrossproductions.yellowbrossextras.entities.DefenderEntity")) {
            try {
                yellowbrossproductionsIntegration.registerModifiers();
                LOGGER.info("Successfully initialized yellowbrossproductions integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize yellowbrossproductions integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("yellowbrossproductions not found, some modifier will not be registered");

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
                OptionalDependencyHandler.isClassPresent("com.c2h6s.etstlib.register.EtSTLibHooks")&&
                OptionalDependencyHandler.isClassPresent("net.wzz.stredgeuniverse.entity.MeteoriteSwordEntity")) {
            try {
                Wzz_Etstlib_SlashBladeIntegration.registerModifiers();
                LOGGER.info("Successfully initialized Wzz and Etstlib and SlashBlade integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Wzz and Etstlib and SlashBlade integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("Wzz and Etstlib and SlashBlade not found, some modifier will not be registered");
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

        if (OptionalDependencyHandler.isClassPresent("moze_intel.projecte.api.capabilities.PECapabilities")) {
            try {
                projecteIntegration.registerModifiers();
                LOGGER.info("Successfully initialized projecte integration");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize projecte integration: {}", e.getMessage());
            }
        } else {
            LOGGER.info("projecte not found, some modifier will not be registered");

        }

    }
}
