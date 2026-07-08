package io.github.jrxmod.devkit.fabric;

import io.github.jrxmod.devkit.config.ConfigSyncManager;
import io.github.jrxmod.devkit.core.DevkitCore;
import io.github.jrxmod.devkit.networking.DevkitNetworking;
import io.github.jrxmod.devkit.registry.DevkitRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevkitMod implements ModInitializer {
    public static final String MOD_ID = "devkit-api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = "0.1.0-alpha+1.21.1";
    public static final String AUTHOR = "jrxmod";

    @Override
    public void onInitialize() {
        LOGGER.info("[DevKit API] by {} Initializing v{} for Minecraft 1.21.1 / 1.21.8 dual-branch", AUTHOR, VERSION);
        DevkitCore.init();
        DevkitRegistry.init();
        DevkitNetworking.init();
        // Config sync is optional – initialize lazily on first use,
        // but pre-warm here to register internal sync packet.
        try {
            ConfigSyncManager.init();
        } catch (Throwable t) {
            LOGGER.warn("Config sync manager failed to initialize early, will retry lazily", t);
        }
        // Attempt auto-bootstrap for any KRegister instances created during mod init
        DevkitRegistry.bootstrapAll();
        LOGGER.info("[DevKit API] Core modules loaded. Ready for dependent mods.");
    }
}
