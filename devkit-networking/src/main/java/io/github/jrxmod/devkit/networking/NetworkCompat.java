package io.github.jrxmod.devkit.networking;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.Method;

/**
 * Version compatibility shim for networking API differences
 * between Minecraft 1.21.1 LTS and 1.21.5+ / 1.21.8+.
 * <p>
 * Specifically handles:
 * <ul>
 *   <li>{@code ServerPlayNetworking.reconfigure()} – added in 1.21.5</li>
 *   <li>Payload API minor signature changes</li>
 * </ul>
 * All calls degrade gracefully on 1.21.1.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class NetworkCompat {
    private static final Method RECONFIGURE_METHOD;
    private static final boolean HAS_RECONFIGURE;

    static {
        Method m;
        boolean has;
        try {
            m = ServerPlayNetworking.class.getMethod("reconfigure", ServerPlayerEntity.class);
            has = true;
        } catch (NoSuchMethodException e) {
            m = null;
            has = false;
        }
        RECONFIGURE_METHOD = m;
        HAS_RECONFIGURE = has;
        DevkitCore.LOGGER.debug("NetworkCompat: reconfigure() available = {}", has);
    }

    private NetworkCompat() {}

    /**
     * Puts the player back into configuration phase if supported
     * by the current Minecraft / Fabric API version.
     * <p>
     * No-op on 1.21.1 LTS. Active on 1.21.5+.
     *
     * @param player target server player
     * @return true if reconfigure was invoked, false if unsupported
     */
    public static boolean reconfigureIfAvailable(ServerPlayerEntity player) {
        if (!HAS_RECONFIGURE || RECONFIGURE_METHOD == null) {
            return false;
        }
        try {
            RECONFIGURE_METHOD.invoke(null, player);
            return true;
        } catch (ReflectiveOperationException e) {
            DevkitCore.LOGGER.debug("reconfigure() invocation failed", e);
            return false;
        }
    }

    /**
     * @return true if the current runtime supports
     *         {@code ServerPlayNetworking.reconfigure()}
     */
    public static boolean supportsReconfigure() {
        return HAS_RECONFIGURE;
    }

    /**
     * Returns a human-readable capability string for diagnostics.
     * Useful for F3 debug overlays or /devkit info commands.
     *
     * @return capability summary, e.g. "1.21.1-compat" or "1.21.8+"
     */
    public static String getNetworkCapabilities() {
        return HAS_RECONFIGURE ? "1.21.5+" : "1.21.1-compat";
    }
}
