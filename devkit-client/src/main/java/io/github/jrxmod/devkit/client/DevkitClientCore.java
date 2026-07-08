package io.github.jrxmod.devkit.client;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client-side bootstrap for DevKit modules – common-side entrypoint.
 * <p>
 * This class is referenced by both the dedicated server classpath
 * (where it does nothing) and the client classpath (where it delegates
 * to {@code DevkitClientCoreImpl}). The actual implementation lives in
 * the client-only source set so that direct references to
 * {@code ClientPlayNetworking} and {@code MinecraftClient} never
 * reach the dedicated server's compile classpath.
 *
 * @author jrxmod
 * @since 0.1.0
 */
@Environment(EnvType.CLIENT)
public final class DevkitClientCore {
    private DevkitClientCore() {}

    /**
     * Initializes client-side DevKit subsystems.
     * <p>
     * Safe to call from any environment: on the dedicated server it
     * logs and returns; on the client it delegates to the real
     * implementation class via reflection.
     */
    public static void init() {
        try {
            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
                DevkitCore.LOGGER.info("[DevKit] Running on dedicated server – client init skipped");
                return;
            }
            Class<?> impl = Class.forName("io.github.jrxmod.devkit.client.DevkitClientCoreImpl");
            impl.getMethod("init").invoke(null);
        } catch (ClassNotFoundException e) {
            DevkitCore.LOGGER.warn("DevkitClientCoreImpl not on classpath – client init skipped");
        } catch (Throwable t) {
            DevkitCore.LOGGER.debug("Client init failed: {}", t.toString());
        }
    }
}
