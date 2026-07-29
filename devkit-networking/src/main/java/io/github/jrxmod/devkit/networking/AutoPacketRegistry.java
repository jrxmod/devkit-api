package io.github.jrxmod.devkit.networking;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Explicit registry for {@link SyncedPacket} payload types.
 *
 * <p>Packet classes expose public static {@code ID} and {@code CODEC} fields.
 * The registry validates those fields and wires the correct Fabric payload
 * codec and server receiver. Client receivers are installed by client-only
 * code through registration listeners.</p>
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class AutoPacketRegistry {
    private static final Set<Identifier> REGISTERED = ConcurrentHashMap.newKeySet();
    private static final Map<Identifier, PacketEntry<?>> ENTRIES = new ConcurrentHashMap<>();
    private static final List<Consumer<PacketEntry<?>>> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile boolean initialized;

    private AutoPacketRegistry() {}

    /** Initializes registry bookkeeping. Packet discovery remains explicit. */
    public static void init() {
        if (!initialized) {
            initialized = true;
            DevkitCore.LOGGER.info("[DevKit Networking] Packet registry initialized");
        }
    }

    /**
     * Compatibility alias retained from 0.1. This method does not scan the
     * classpath; dependent mods must register packet classes explicitly.
     */
    @Deprecated
    public static void scanAndRegister() {
        init();
    }

    /** Registers a packet using its {@link AutoPacket} direction. */
    public static <T extends SyncedPacket> void register(Class<T> packetClass) {
        AutoPacket metadata = packetClass.getAnnotation(AutoPacket.class);
        if (metadata == null) {
            throw new IllegalArgumentException("Packet " + packetClass.getName() + " is missing @AutoPacket");
        }
        register(packetClass, metadata.direction());
    }

    /** Registers a packet with an explicit direction. */
    @SuppressWarnings("unchecked")
    public static <T extends SyncedPacket> void register(Class<T> packetClass, AutoPacket.Direction direction) {
        Objects.requireNonNull(packetClass, "packetClass");
        Objects.requireNonNull(direction, "direction");
        init();

        try {
            Field idField = requiredStaticField(packetClass, "ID");
            Field codecField = requiredStaticField(packetClass, "CODEC");
            CustomPayload.Id<T> id = (CustomPayload.Id<T>) idField.get(null);
            PacketCodec<RegistryByteBuf, T> codec = (PacketCodec<RegistryByteBuf, T>) codecField.get(null);
            if (id == null || codec == null) {
                throw new IllegalStateException("Packet ID and CODEC must not be null: " + packetClass.getName());
            }

            validateAnnotationId(packetClass, id.id());
            if (!REGISTERED.add(id.id())) {
                PacketEntry<?> existing = ENTRIES.get(id.id());
                if (existing != null && existing.packetClass().equals(packetClass)
                        && existing.direction() == direction) {
                    DevkitCore.LOGGER.debug("Packet {} already registered", id.id());
                    return;
                }
                throw new IllegalStateException("Duplicate packet id " + id.id() + " for " + packetClass.getName());
            }

            try {
                if (direction.allowsServerToClient()) {
                    PayloadTypeRegistry.playS2C().register(id, codec);
                }
                if (direction.allowsClientToServer()) {
                    PayloadTypeRegistry.playC2S().register(id, codec);
                    ServerPlayNetworking.registerGlobalReceiver(id, (payload, context) ->
                            payload.handle(new ServerNetworkContext(context.player(), context.server())));
                }

                PacketEntry<T> entry = new PacketEntry<>(packetClass, id, direction);
                ENTRIES.put(id.id(), entry);
                for (Consumer<PacketEntry<?>> listener : LISTENERS) {
                    listener.accept(entry);
                }
                DevkitCore.LOGGER.info("Registered network packet: {} [{}]", id.id(), direction);
            } catch (RuntimeException e) {
                REGISTERED.remove(id.id());
                ENTRIES.remove(id.id());
                throw e;
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read public static ID/CODEC fields from "
                    + packetClass.getName(), e);
        }
    }

    /**
     * Adds a callback for packets registered after client initialization.
     * Existing entries are intentionally available through {@link #getEntries()}.
     */
    public static void addRegistrationListener(Consumer<PacketEntry<?>> listener) {
        LISTENERS.add(Objects.requireNonNull(listener, "listener"));
    }

    /** @return immutable snapshot of packet entries */
    public static List<PacketEntry<?>> getEntries() {
        return List.copyOf(ENTRIES.values());
    }

    /** @return immutable snapshot of packet identifiers */
    public static Set<Identifier> getRegisteredIds() {
        return Set.copyOf(REGISTERED);
    }

    private static Field requiredStaticField(Class<?> packetClass, String name) throws NoSuchFieldException {
        Field field = packetClass.getField(name);
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalStateException(packetClass.getName() + "." + name + " must be static");
        }
        return field;
    }

    private static void validateAnnotationId(Class<?> packetClass, Identifier actual) {
        AutoPacket metadata = packetClass.getAnnotation(AutoPacket.class);
        if (metadata == null) {
            return; // Explicit-direction overload remains usable for internal compatibility.
        }
        String value = metadata.value().trim();
        int slash = value.indexOf('/');
        if (slash > 0 && value.indexOf(':') < 0) {
            value = value.substring(0, slash) + ':' + value.substring(slash + 1);
        }
        Identifier declared = Identifier.tryParse(value);
        if (declared == null) {
            throw new IllegalArgumentException("Invalid @AutoPacket id '" + metadata.value()
                    + "' on " + packetClass.getName());
        }
        if (!declared.equals(actual)) {
            throw new IllegalArgumentException("@AutoPacket id " + declared + " does not match ID "
                    + actual + " on " + packetClass.getName());
        }
    }

    /** Description shared with client-only receiver setup. */
    public record PacketEntry<T extends SyncedPacket>(
            Class<T> packetClass,
            CustomPayload.Id<T> id,
            AutoPacket.Direction direction
    ) {}

    private record ServerNetworkContext(
            ServerPlayerEntity player,
            net.minecraft.server.MinecraftServer server
    ) implements SyncedPacket.NetworkContext {
        @Override public boolean isClient() { return false; }
        @Override public net.minecraft.entity.player.PlayerEntity getPlayer() { return player; }
        @Override public void queue(Runnable task) { server.execute(task); }
    }
}
