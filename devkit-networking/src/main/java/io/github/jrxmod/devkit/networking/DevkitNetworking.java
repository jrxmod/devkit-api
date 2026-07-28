package io.github.jrxmod.devkit.networking;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/** Server/common-side networking helpers backed by Fabric's Payload API. */
public final class DevkitNetworking {
    private DevkitNetworking() {}

    public static void init() {
        AutoPacketRegistry.init();
        DevkitCore.LOGGER.info("[DevKit] Networking initialized");
    }

    public static <T extends SyncedPacket> boolean sendToPlayer(ServerPlayerEntity player, T packet) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(packet, "packet");
        if (!ServerPlayNetworking.canSend(player, packet.getId())) {
            return false;
        }
        ServerPlayNetworking.send(player, packet);
        return true;
    }

    public static <T extends SyncedPacket> int sendToTracking(ServerWorld world, BlockPos pos, T packet) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(pos, "pos");
        int sent = 0;
        for (ServerPlayerEntity player : PlayerLookup.tracking(world, pos)) {
            if (sendToPlayer(player, packet)) {
                sent++;
            }
        }
        return sent;
    }

    public static <T extends SyncedPacket> int sendToAll(Iterable<ServerPlayerEntity> players, T packet) {
        Objects.requireNonNull(players, "players");
        int sent = 0;
        for (ServerPlayerEntity player : players) {
            if (sendToPlayer(player, packet)) {
                sent++;
            }
        }
        return sent;
    }

    /**
     * Compatibility bridge for 0.1 consumers. New client code should call
     * client-only {@code DevkitClientNetworking.sendToServer} directly.
     */
    @Deprecated
    public static <T extends SyncedPacket> void sendToServer(T packet) {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()
                != net.fabricmc.api.EnvType.CLIENT) {
            throw new IllegalStateException("sendToServer is only available on a physical client");
        }
        try {
            Class<?> type = Class.forName("io.github.jrxmod.devkit.networking.DevkitClientNetworking");
            type.getMethod("sendToServer", SyncedPacket.class).invoke(null, packet);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke client networking", e);
        }
    }
}
