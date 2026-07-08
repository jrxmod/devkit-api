package io.github.jrxmod.devkit.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core bootstrap and global constants for DevKit API.
 * <p>
 * Provides mod identifier, logger instance, and API versioning.
 * All DevKit modules depend on this core module.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class DevkitCore {
    /** Mod identifier used across all DevKit modules. */
    public static final String MOD_ID = "devkit-api";

    /** Shared SLF4J logger instance. */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Current DevKit API version – increment on breaking changes. */
    public static final int API_VERSION = 1;

    /** Primary mod author. */
    public static final String AUTHOR = "jrxmod";

    private static boolean initialized = false;

    private DevkitCore() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("DevKit Core API v{} by {} initialized", API_VERSION, AUTHOR);
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
