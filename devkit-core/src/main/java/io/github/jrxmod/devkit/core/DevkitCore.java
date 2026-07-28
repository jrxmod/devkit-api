package io.github.jrxmod.devkit.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared constants and idempotent bootstrap for all DevKit modules. */
public final class DevkitCore {
    public static final String MOD_ID = "devkit-api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int API_VERSION = 2;
    public static final String AUTHOR = "jrxmod";

    private static volatile boolean initialized;

    private DevkitCore() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        LOGGER.info("DevKit API {} by {} initialized", API_VERSION, AUTHOR);
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
