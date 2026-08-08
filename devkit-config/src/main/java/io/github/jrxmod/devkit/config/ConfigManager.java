package io.github.jrxmod.devkit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * JSON configuration manager with load-or-create semantics and optional
 * server-authoritative synchronization through {@link SyncedConfig}.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class ConfigManager {
    private static final Pattern MOD_ID = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern FILE_NAME = Pattern.compile("[a-z0-9_.-]+");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private ConfigManager() {}

    /**
     * Loads an existing configuration or creates and saves a default instance.
     * A malformed existing file is left untouched and a default in-memory
     * instance is returned, so user data is never silently overwritten.
     *
     * @param clazz configuration class with an accessible no-args constructor
     * @param modId owning mod namespace
     * @param name file name without extension
     * @param <T> configuration type
     * @return live configuration instance
     */
    public static <T> T loadOrCreate(Class<T> clazz, String modId, String name) {
        Objects.requireNonNull(clazz, "clazz");
        Path path = pathFor(modId, name);

        try {
            if (Files.isRegularFile(path)) {
                T instance = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), clazz);
                if (instance == null) {
                    throw new IOException("GSON returned null");
                }
                maybeRegisterSync(instance, clazz, name);
                return instance;
            }

            T instance = newInstance(clazz);
            save(instance, path);
            maybeRegisterSync(instance, clazz, name);
            return instance;
        } catch (Exception e) {
            DevkitCore.LOGGER.error("Configuration load failed: {}. Using in-memory defaults; the file was not overwritten.", path, e);
            T fallback = newInstance(clazz);
            maybeRegisterSync(fallback, clazz, name);
            return fallback;
        }
    }

    /**
     * Reloads a configuration from disk, returning a fresh instance.
     * The caller decides when to replace the live reference.
     * A malformed file is left untouched and the method returns a default
     * in-memory instance.
     */
    public static <T> T reload(Class<T> clazz, String modId, String name) {
        Objects.requireNonNull(clazz, "clazz");
        Path path = pathFor(modId, name);
        try {
            if (Files.isRegularFile(path)) {
                T instance = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), clazz);
                if (instance == null) throw new IOException("GSON returned null");
                maybeRegisterSync(instance, clazz, name);
                DevkitCore.LOGGER.debug("Reloaded configuration: {}", path);
                return instance;
            }
            DevkitCore.LOGGER.warn("Configuration file not found for reload, creating default: {}", path);
            T instance = newInstance(clazz);
            save(instance, path);
            maybeRegisterSync(instance, clazz, name);
            return instance;
        } catch (Exception e) {
            DevkitCore.LOGGER.error("Configuration reload failed: {}. Using in-memory defaults.", path, e);
            T fallback = newInstance(clazz);
            maybeRegisterSync(fallback, clazz, name);
            return fallback;
        }
    }

    /**
     * Atomically persists a configuration object where the filesystem permits.
     *
     * @param object configuration instance
     * @param path destination file
     */
    public static void save(Object object, Path path) {
        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(path, "path");

        Path absolute = path.toAbsolutePath().normalize();
        Path parent = absolute.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Configuration path has no parent: " + path);
        }

        Path temporary = null;
        try {
            Files.createDirectories(parent);
            temporary = Files.createTempFile(parent, absolute.getFileName().toString(), ".tmp");
            Files.writeString(temporary, GSON.toJson(object), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            DevkitCore.LOGGER.error("Configuration save failed: {}", absolute, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Best-effort cleanup after a failed write or move.
                }
            }
        }
    }

    /**
     * Saves to {@code config/<modId>/<name>.json}.
     */
    public static void save(Object object, String modId, String name) {
        save(object, pathFor(modId, name));
    }

    /**
     * Resolves and validates the conventional path for a configuration file.
     */
    public static Path pathFor(String modId, String name) {
        validateSegment("modId", modId, MOD_ID);
        validateSegment("name", name, FILE_NAME);
        return FabricLoader.getInstance().getConfigDir()
                .resolve(modId)
                .resolve(name + ".json")
                .normalize();
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            if (!constructor.canAccess(null)) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to instantiate config class: " + clazz.getName(), e);
        }
    }

    private static void validateSegment(String label, String value, Pattern pattern) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid config " + label + ": " + value);
        }
    }

    private static <T> void maybeRegisterSync(T instance, Class<T> clazz, String name) {
        SyncedConfig metadata = clazz.getAnnotation(SyncedConfig.class);
        if (metadata == null) {
            return;
        }

        ConfigSyncManager.init();
        ConfigSyncManager.register(metadata.value(), name, instance, clazz, metadata.serverAuthoritative());
    }
}
