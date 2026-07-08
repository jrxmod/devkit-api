package io.github.jrxmod.devkit.networking;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

/**
 * Networking facade for DevKit API (server / common side).
 * <p>
 * Provides unified send helpers for S2C communication, backed by Fabric's
 * Payload API (1.21.1+). For C2S sending from a client, see the
 * client-only {@link DevkitClientNetworking}.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class DevkitNetworking {
    private static final Logger LOGGER = DevkitCore.LOGGER;

    private DevkitNetworking() {}

    /**
     * Initializes the networking subsystem.
     */
    public static void init() {
        LOGGER.info("[DevKit] Auto-Networking initialized");
        AutoPacketRegistry.scanAndRegister();
    }

    /**
     * Sends a synced packet to a specific player (S2C).
     *
     * @param player target player
     * @param packet payload to send
     * @param <T> packet type
     */
    public static <T extends SyncedPacket> void sendToPlayer(ServerPlayerEntity player, T packet) {
        if (ServerPlayNetworking.canSend(player, packet.getId())) {
            ServerPlayNetworking.send(player, packet);
        }
    }

    /**
     * Broadcasts a packet to all players tracking a block position.
     *
     * @param world server world
     * @param pos tracked position
     * @param packet payload
     * @param <T> packet type
     */
    public static <T extends SyncedPacket> void sendToTracking(ServerWorld world, BlockPos pos, T packet) {
        for (ServerPlayerEntity player : PlayerLookup.tracking(world, pos)) {
            sendToPlayer(player, packet);
        }
    }

    /**
     * Broadcasts a packet to all players on the server.
     *
     * @param players iterable player list, typically {@code server.getPlayerManager().getPlayerList()}
     * @param packet payload
     * @param <T> packet type
     */
    public static <T extends SyncedPacket> void sendToAll(Iterable<ServerPlayerEntity> players, T packet) {
        for (ServerPlayerEntity player : players) {
            sendToPlayer(player, packet);
        }
    }

    /**
     * Sends a packet from the local client to the connected server (C2S).
     * <p>
     * This method is a no-op when called on a dedicated server because
     * Fabric's {@code ClientPlayNetworking} is only present in the client
     * classpath. We detect the environment via
     * {@link net.fabricmc.loader.api.FabricLoader#getEnvironmentType()} to
     * avoid {@code NoClassDefFoundError}.
     *
     * @param packet payload
     * @param <T> packet type
     */
    public static <T extends SyncedPacket> void sendToServer(T packet) {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()
                != net.fabricmc.api.EnvType.CLIENT) {
            DevkitCore.LOGGER.debug("sendToServer() called on a non-client environment – ignoring");
            return;
        }
        try {
            // Look up the class reflectively to keep Fabric's client networking
            // classes out of the dedicated server's classpath.
            Class<?> cls = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
            cls.getMethod("send", net.minecraft.network.packet.CustomPayload.class)
                    .invoke(null, packet);
        } catch (ClassNotFoundException e) {
            DevkitCore.LOGGER.error("ClientPlayNetworking not found on the classpath", e);
        } catch (ReflectiveOperationException e) {
            DevkitCore.LOGGER.error("Failed to invoke ClientPlayNetworking.send()", e);
        }
    }
}
