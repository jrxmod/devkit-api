package io.github.jrxmod.devkit.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Objects;

/** Client-only C2S networking helpers. */
@Environment(EnvType.CLIENT)
public final class DevkitClientNetworking {
    private DevkitClientNetworking() {}

    /**
     * Sends a payload to the connected server.
     *
     * @throws IllegalStateException if there is no connection or the server did
     * not declare support for the payload
     */
    public static <T extends SyncedPacket> void sendToServer(T packet) {
        Objects.requireNonNull(packet, "packet");
        if (!ClientPlayNetworking.canSend(packet.getId())) {
            throw new IllegalStateException("The connected server cannot receive " + packet.getId().id());
        }
        ClientPlayNetworking.send(packet);
    }
}
