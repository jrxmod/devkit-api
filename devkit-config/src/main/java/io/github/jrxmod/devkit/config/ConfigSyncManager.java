package io.github.jrxmod.devkit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jrxmod.devkit.core.DevkitCore;
import io.github.jrxmod.devkit.networking.AutoPacket;
import io.github.jrxmod.devkit.networking.AutoPacketRegistry;
import io.github.jrxmod.devkit.networking.DevkitNetworking;
import io.github.jrxmod.devkit.networking.SyncedPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Synchronizes explicitly registered JSON configurations from a server to its
 * clients while preserving each client's live configuration object identity.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class ConfigSyncManager {
    /** Conservative limit below Minecraft's normal encoded string ceiling. */
    public static final int MAX_JSON_CHARACTERS = 30_000;

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Map<String, Holder<?>> REGISTRY = new ConcurrentHashMap<>();
    private static volatile boolean initialized;

    private ConfigSyncManager() {}

    /** Initializes the payload and the server join listener once. */
    public static synchronized void init() {
        if (initialized) {
            return;
        }

        AutoPacketRegistry.register(ConfigSyncPacket.class, AutoPacket.Direction.S2C);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (Holder<?> holder : REGISTRY.values()) {
                if (holder.serverAuthoritative()) {
                    sendTo(handler.player, holder);
                }
            }
        });
        initialized = true;
        DevkitCore.LOGGER.info("[DevKit] Config sync manager initialized ({} configs)", REGISTRY.size());
    }

    /**
     * Registers the conventional {@code main} config for compatibility with
     * the original 0.1 API.
     */
    public static <T> void register(String modId, T instance, Class<T> clazz, boolean serverAuthoritative) {
        register(modId, "main", instance, clazz, serverAuthoritative);
    }

    /** Registers a uniquely named configuration instance. */
    public static <T> void register(String modId, String name, T instance, Class<T> clazz,
                                    boolean serverAuthoritative) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(clazz, "clazz");
        String configId = configId(modId, name);
        Holder<T> replacement = new Holder<>(configId, instance, clazz, serverAuthoritative);
        Holder<?> previous = REGISTRY.put(configId, replacement);
        if (previous != null && previous.instance() != instance) {
            DevkitCore.LOGGER.debug("Replaced synced config registration: {}", configId);
        } else {
            DevkitCore.LOGGER.debug("Registered synced config: {} (authoritative={})", configId, serverAuthoritative);
        }
    }

    /** Registers an annotated instance as its conventional {@code main} config. */
    @SuppressWarnings("unchecked")
    public static <T> void register(T instance) {
        Objects.requireNonNull(instance, "instance");
        Class<T> clazz = (Class<T>) instance.getClass();
        SyncedConfig metadata = clazz.getAnnotation(SyncedConfig.class);
        if (metadata == null) {
            throw new IllegalArgumentException("Config class " + clazz.getName() + " is missing @SyncedConfig");
        }
        register(metadata.value(), "main", instance, clazz, metadata.serverAuthoritative());
    }

    /**
     * Broadcasts one config when passed a full {@code namespace:name} key, or
     * every config owned by a namespace when passed only {@code namespace}.
     */
    public static void broadcastToAll(MinecraftServer server, String configOrModId) {
        if (server == null || configOrModId == null) {
            return;
        }
        for (Holder<?> holder : matching(configOrModId)) {
            if (!holder.serverAuthoritative()) {
                continue;
            }
            ConfigSyncPacket packet = packetFor(holder);
            if (packet == null) {
                continue;
            }
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                DevkitNetworking.sendToPlayer(player, packet);
            }
        }
    }

    /** @return immutable snapshot of registered synchronization keys */
    public static List<String> getRegisteredConfigIds() {
        return List.copyOf(REGISTRY.keySet());
    }

    private static List<Holder<?>> matching(String configOrModId) {
        Holder<?> exact = REGISTRY.get(configOrModId);
        if (exact != null) {
            return List.of(exact);
        }

        String prefix = configOrModId + ":";
        List<Holder<?>> result = new ArrayList<>();
        for (Map.Entry<String, Holder<?>> entry : REGISTRY.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    private static void sendTo(ServerPlayerEntity player, Holder<?> holder) {
        ConfigSyncPacket packet = packetFor(holder);
        if (packet != null) {
            DevkitNetworking.sendToPlayer(player, packet);
        }
    }

    private static ConfigSyncPacket packetFor(Holder<?> holder) {
        String json = GSON.toJson(holder.instance());
        if (json.length() > MAX_JSON_CHARACTERS) {
            DevkitCore.LOGGER.error("Synced config {} is too large ({} characters, maximum {})",
                    holder.configId(), json.length(), MAX_JSON_CHARACTERS);
            return null;
        }
        return new ConfigSyncPacket(holder.configId(), json);
    }

    private static String configId(String modId, String name) {
        Identifier id = Identifier.of(modId, name);
        return id.toString();
    }

    private record Holder<T>(String configId, T instance, Class<T> type, boolean serverAuthoritative) {}

    /** Internal S2C configuration payload. */
    @AutoPacket(value = "devkit-api:config_sync", direction = AutoPacket.Direction.S2C)
    public record ConfigSyncPacket(String configId, String json) implements SyncedPacket {
        public static final CustomPayload.Id<ConfigSyncPacket> ID =
                new CustomPayload.Id<>(Identifier.of("devkit-api", "config_sync"));

        public static final PacketCodec<RegistryByteBuf, ConfigSyncPacket> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, ConfigSyncPacket::configId,
                PacketCodecs.STRING, ConfigSyncPacket::json,
                ConfigSyncPacket::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public void handle(NetworkContext context) {
            if (!context.isClient() || json.length() > MAX_JSON_CHARACTERS) {
                return;
            }

            context.queue(() -> {
                Holder holder = REGISTRY.get(configId);
                if (holder == null || !holder.serverAuthoritative()) {
                    return;
                }

                try {
                    Object fresh = GSON.fromJson(json, holder.type());
                    if (fresh == null) {
                        DevkitCore.LOGGER.warn("Synced config {} deserialized to null", configId);
                        return;
                    }
                    copyMutableFields(holder.type(), fresh, holder.instance());
                    DevkitCore.LOGGER.debug("Applied synced config: {}", configId);
                } catch (RuntimeException | ReflectiveOperationException e) {
                    DevkitCore.LOGGER.error("Failed to apply synced config {}", configId, e);
                }
            });
        }
    }

    private static void copyMutableFields(Class<?> type, Object source, Object destination)
            throws IllegalAccessException {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }
                if (!field.canAccess(source)) {
                    field.setAccessible(true);
                }
                field.set(destination, field.get(source));
            }
            current = current.getSuperclass();
        }
    }
}
