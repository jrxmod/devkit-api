package io.github.jrxmod.devkit.client;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Physical-client implementation of the DevKit bootstrap. */
@Environment(EnvType.CLIENT)
public final class DevkitClientCoreImpl {
    private static boolean initialized;

    private DevkitClientCoreImpl() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            Class<?> registry = Class.forName(
                    "io.github.jrxmod.devkit.networking.DevkitClientPacketRegistry");
            registry.getMethod("init").invoke(null);
            initialized = true;
            DevkitCore.LOGGER.info("[DevKit] Client module initialized");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Client networking module is missing or failed to initialize", e);
        }
    }
}
