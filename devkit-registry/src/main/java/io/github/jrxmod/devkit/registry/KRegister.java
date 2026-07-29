package io.github.jrxmod.devkit.registry;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Ordered, type-safe container for deferred vanilla registry entries.
 *
 * <p>Create it with a key from {@code RegistryKeys}, declare entries, then call
 * {@link #bootstrap(Registry)} during the owning mod's common initializer.</p>
 *
 * @param <T> registry value type
 * @author jrxmod
 * @since 0.1.0
 */
public final class KRegister<T> {
    private final String modId;
    private final RegistryKey<Registry<T>> registryKey;
    private final Map<Identifier, PendingEntry<T>> entries = new LinkedHashMap<>();
    private State state = State.OPEN;

    private KRegister(String modId, RegistryKey<Registry<T>> registryKey) {
        if (modId == null || !modId.matches("[a-z0-9_.-]+")) {
            throw new IllegalArgumentException("Invalid mod id: " + modId);
        }
        this.modId = modId;
        this.registryKey = Objects.requireNonNull(registryKey, "registryKey");
        DevkitRegistry.track(this);
    }

    public static <T> KRegister<T> create(String modId, RegistryKey<Registry<T>> registryKey) {
        return new KRegister<>(modId, registryKey);
    }

    /**
     * Registers a value factory that does not need its registry key.
     *
     * <p>For blocks and items on Minecraft 1.21.2 or newer, prefer
     * {@link #register(String, Function)} because their settings must receive
     * the key before the object is constructed.</p>
     */
    public RegistrySupplier<T> register(String path, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return register(path, ignored -> supplier.get());
    }

    /**
     * Registers a value factory and supplies the exact key that will be used by
     * the target registry. This keeps one declaration compatible with the
     * registry-key-aware settings introduced in Minecraft 1.21.2.
     *
     * <pre>{@code
     * ITEMS.register("ruby", key ->
     *     new Item(new Item.Settings().registryKey(key)));
     * }</pre>
     */
    public synchronized RegistrySupplier<T> register(
            String path,
            Function<RegistryKey<T>, ? extends T> factory
    ) {
        if (state != State.OPEN) {
            throw new IllegalStateException("KRegister " + modId + " for " + registryKey.getValue()
                    + " is already being bootstrapped or frozen");
        }
        Objects.requireNonNull(factory, "factory");
        Identifier id = Identifier.of(modId, path);
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate registration: " + id);
        }

        RegistryKey<T> entryKey = RegistryKey.of(registryKey, id);
        RegistrySupplier<T> holder = new RegistrySupplier<>(id);
        entries.put(id, new PendingEntry<>(entryKey, factory, holder));
        return holder;
    }

    /**
     * Registers all pending values. Successfully registered entries remain
     * bound if a later factory fails, and a retry skips those entries.
     */
    public synchronized void bootstrap(Registry<T> registry) {
        Objects.requireNonNull(registry, "registry");
        if (state == State.FROZEN) {
            return;
        }
        if (state == State.BOOTSTRAPPING) {
            throw new IllegalStateException("Recursive bootstrap for " + registryKey.getValue());
        }

        state = State.BOOTSTRAPPING;
        int registered = 0;
        try {
            for (Map.Entry<Identifier, PendingEntry<T>> entry : entries.entrySet()) {
                PendingEntry<T> pending = entry.getValue();
                if (pending.holder().isPresent()) {
                    continue;
                }
                T value = Objects.requireNonNull(pending.factory().apply(pending.key()),
                        "Registry factory returned null for " + entry.getKey());
                Registry.register(registry, pending.key(), value);
                pending.holder().bind(value);
                registered++;
            }
            state = State.FROZEN;
            DevkitCore.LOGGER.debug("KRegister bootstrap: {} -> {} new entries", registryKey.getValue(), registered);
        } catch (RuntimeException e) {
            state = State.OPEN;
            throw new IllegalStateException("Failed to bootstrap registry " + registryKey.getValue()
                    + " for " + modId, e);
        }
    }

    public RegistryKey<Registry<T>> getRegistryKey() {
        return registryKey;
    }

    public String getModId() {
        return modId;
    }

    public synchronized List<RegistrySupplier<T>> getEntries() {
        return entries.values().stream().map(PendingEntry::holder).toList();
    }

    public synchronized boolean isFrozen() {
        return state == State.FROZEN;
    }

    private record PendingEntry<T>(
            RegistryKey<T> key,
            Function<RegistryKey<T>, ? extends T> factory,
            RegistrySupplier<T> holder
    ) {}

    private enum State {
        OPEN,
        BOOTSTRAPPING,
        FROZEN
    }
}
