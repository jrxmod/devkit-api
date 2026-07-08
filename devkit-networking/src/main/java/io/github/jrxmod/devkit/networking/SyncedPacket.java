package io.github.jrxmod.devkit.networking;

import net.minecraft.network.packet.CustomPayload;

/**
 * Base interface for auto-registered network packets.
 * <p>
 * Implementations should be Java records and provide:
 * - public static final CustomPayload.Id<T> ID
 * - public static final PacketCodec<RegistryByteBuf, T> CODEC
 * <p>
 * Example:
 * <pre>
 * {@code
 * @AutoPacket("example/sync")
 * public record EnergySyncPacket(BlockPos pos, int energy) implements SyncedPacket {
 *     public static final CustomPayload.Id<EnergySyncPacket> ID = 
 *         new CustomPayload.Id<>(Identifier.of("mymod", "energy_sync"));
 *     public static final PacketCodec<RegistryByteBuf, EnergySyncPacket> CODEC = 
 *         PacketCodec.tuple(
 *             BlockPos.PACKET_CODEC, EnergySyncPacket::pos,
 *             PacketCodecs.VAR_INT, EnergySyncPacket::energy,
 *             EnergySyncPacket::new
 *         );
 *     public Id<? extends CustomPayload> getId() { return ID; }
 * }
 * }
 * </pre>
 *
 * @author jrxmod
 * @since 0.1.0
 */
public interface SyncedPacket extends CustomPayload {

    /**
     * Server-side or client-side handler, invoked on network thread.
     * Implementations should schedule to main thread if accessing world state.
     *
     * @param context execution context providing side information
     */
    default void handle(NetworkContext context) {
        // no-op by default
    }

    /**
     * Network execution context.
     */
    interface NetworkContext {
        /**
         * @return true if executing on logical client
         */
        boolean isClient();

        /**
         * @return the player associated with this packet, if available
         */
        net.minecraft.entity.player.PlayerEntity getPlayer();

        /**
         * Schedule a task on the main game thread.
         *
         * @param task runnable to execute
         */
        void queue(Runnable task);
    }
}
