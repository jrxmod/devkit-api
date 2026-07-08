package io.github.jrxmod.devkit.networking;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for {@link SyncedPacket} types.
 * <p>
 * Handles payload codec registration and server-side global receiver
 * binding. Client-side registration is delegated to
 * {@link DevkitClientPacketRegistry} which lives in the {@code client}
 * source set to keep Fabric's {@code ClientPlayNetworking} off the
 * dedicated server classpath.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class AutoPacketRegistry {
    private static final Logger LOGGER = DevkitCore.LOGGER;
    private static final Set<Identifier> REGISTERED = ConcurrentHashMap.newKeySet();
    private static final Map<Identifier, PacketEntry<?>> ENTRIES = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    private AutoPacketRegistry() {}

    /**
     * Initializes the networking subsystem.
     * Invoked automatically by {@link DevkitNetworking}.
     */
    public static void scanAndRegister() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("[DevKit Networking] AutoPacket registry initialized");
    }

    /**
     * Registers a synced packet type, resolving direction from {@link AutoPacket} annotation.
     *
     * @param packetClass packet implementation class
     * @param <T> packet type
     */
    public static <T extends SyncedPacket> void register(Class<T> packetClass) {
        AutoPacket meta = packetClass.getAnnotation(AutoPacket.class);
        AutoPacket.Direction direction = (meta != null) ? meta.direction() : AutoPacket.Direction.BIDIRECTIONAL;
        if (meta == null) {
            LOGGER.warn("Registering {} without @AutoPacket – defaulting to BIDIRECTIONAL", packetClass.getName());
        }
        register(packetClass, direction);
    }

    /**
     * Registers a synced packet with explicit direction control.
     *
     * @param packetClass packet type
     * @param direction network direction
     * @param <T> packet type
     */
    @SuppressWarnings("unchecked")
    public static <T extends SyncedPacket> void register(Class<T> packetClass, AutoPacket.Direction direction) {
        try {
            Field idField = packetClass.getField("ID");
            Field codecField = packetClass.getField("CODEC");

            CustomPayload.Id<T> id = (CustomPayload.Id<T>) idField.get(null);
            PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) codecField.get(null);

            if (!REGISTERED.add(id.id())) {
                LOGGER.debug("Packet {} already registered, skipping", id.id());
                return;
            }

            // Codec registration – Fabric requires explicit S2C / C2S separation
            if (direction == AutoPacket.Direction.S2C || direction == AutoPacket.Direction.BIDIRECTIONAL) {
                PayloadTypeRegistry.playS2C().register(id, codec);
            }
            if (direction == AutoPacket.Direction.C2S || direction == AutoPacket.Direction.BIDIRECTIONAL) {
                PayloadTypeRegistry.playC2S().register(id, codec);
            }

            // Server-side receiver (C2S only – clients send to server)
            if (direction == AutoPacket.Direction.C2S || direction == AutoPacket.Direction.BIDIRECTIONAL) {
                ServerPlayNetworking.registerGlobalReceiver(id, (payload, context) -> {
                    if (payload instanceof SyncedPacket synced) {
                        synced.handle(new ServerNetworkContext(context.player()));
                    }
                });
            }

            ENTRIES.put(id.id(), new PacketEntry<>(packetClass, id, direction));
            LOGGER.info("Registered network packet: {} [{}]", id.id(), direction);

        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("SyncedPacket " + packetClass.getName()
                    + " must expose public static final CustomPayload.Id<?> ID and PacketCodec<?,?> CODEC", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access ID / CODEC fields in " + packetClass.getName(), e);
        }
    }

    /**
     * Returns the registered server-side packet entries.
     * Used by the client entrypoint to auto-wire S2C receivers.
     *
     * @return immutable view of registered entries
     */
    public static List<PacketEntry<?>> getEntries() {
        return Collections.unmodifiableList(List.copyOf(ENTRIES.values()));
    }

    /**
     * Returns an immutable snapshot of registered packet identifiers.
     * Useful for debugging and diagnostics.
     *
     * @return registered packet IDs
     */
    public static Set<Identifier> getRegisteredIds() {
        return Set.copyOf(REGISTERED);
    }

    // ------------------------------------------------------------------------
    // Internal structures
    // ------------------------------------------------------------------------

    /**
     * Visible record describing a registered packet. The {@code packetClass}
     * reference is intentionally typed as a raw class so client-only code
     * can iterate without forcing a common dependency on Fabric's
     * {@code ClientPlayNetworking}.
     */
    public record PacketEntry<T extends SyncedPacket>(
            Class<T> packetClass,
            CustomPayload.Id<T> id,
            AutoPacket.Direction direction
    ) {}

    private record ServerNetworkContext(ServerPlayerEntity player)
            implements SyncedPacket.NetworkContext {
        @Override public boolean isClient() { return false; }
        @Override public net.minecraft.entity.player.PlayerEntity getPlayer() { return player; }
        @Override public void queue(Runnable task) { player.getServer().execute(task); }
    }
}
