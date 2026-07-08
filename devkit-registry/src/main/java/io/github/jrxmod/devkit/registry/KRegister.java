package io.github.jrxmod.devkit.registry;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fluent registry container for Fabric 1.21.x.
 * <p>
 * Eliminates repetitive registry boilerplate by collecting suppliers
 * during mod construction and bootstrapping them at the appropriate
 * registry entrypoint.
 * <p>
 * Example:
 * <pre>{@code
 * public static final KRegister<Item> ITEMS = KRegister.create("mymod", Registries.ITEM);
 * public static final RegistrySupplier<Item> RUBY = ITEMS.register("ruby",
 *     () -> new Item(new Item.Settings()));
 * }</pre>
 *
 * @param <T> registry value type
 * @author jrxmod
 * @since 0.1.0
 */
public final class KRegister<T> {
    private final String modId;
    private final RegistryKey<Registry<T>> registryKey;
    private final Map<Identifier, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final List<RegistrySupplier<T>> holders = new ArrayList<>();
    private boolean frozen = false;

    private KRegister(String modId, RegistryKey<Registry<T>> registryKey) {
        this.modId = modId;
        this.registryKey = registryKey;
        DevkitRegistry.track(this);
    }

    /**
     * Creates a new typed registry container.
     *
     * @param modId mod namespace
     * @param registryKey target registry key
     * @param <T> value type
     * @return new register instance
     */
    public static <T> KRegister<T> create(String modId, RegistryKey<Registry<T>> registryKey) {
        return new KRegister<>(modId, registryKey);
    }

    /**
     * Registers a new entry.
     *
     * @param path  registry path (no namespace)
     * @param supplier value factory, invoked once during bootstrap
     * @return typed holder providing lazy access
     * @throws IllegalStateException if the register has already been bootstrapped
     */
    public RegistrySupplier<T> register(String path, Supplier<? extends T> supplier) {
        if (frozen) {
            throw new IllegalStateException("KRegister " + modId + ":" + registryKey.getValue() + " is already frozen");
        }
        Identifier id = Identifier.of(modId, path);
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate registration: " + id);
        }
        entries.put(id, supplier);
        RegistrySupplier<T> holder = new RegistrySupplier<>(id);
        holders.add(holder);
        return holder;
    }

    /**
     * Bootstraps all collected entries into the target registry.
     * Safe to call multiple times – subsequent calls are ignored.
     *
     * @param registry target live registry instance
     */
    public void bootstrap(Registry<T> registry) {
        if (frozen) return;
        int count = 0;
        for (Map.Entry<Identifier, Supplier<? extends T>> e : entries.entrySet()) {
            T value = e.getValue().get();
            Registry.register(registry, e.getKey(), value);
            // inject into corresponding holder
            holders.stream()
                .filter(h -> h.getId().equals(e.getKey()))
                .findFirst()
                .ifPresent(h -> h.bind(value));
            count++;
        }
        frozen = true;
        DevkitCore.LOGGER.debug("KRegister bootstrap: {} -> {} entries", registryKey.getValue(), count);
    }

    /**
     * @return registry key backing this container
     */
    public RegistryKey<Registry<T>> getRegistryKey() {
        return registryKey;
    }

    /**
     * @return owning mod namespace
     */
    public String getModId() {
        return modId;
    }

    /**
     * @return immutable entry holders
     */
    public List<RegistrySupplier<T>> getEntries() {
        return List.copyOf(holders);
    }

    /**
     * @return true if bootstrap() has been invoked
     */
    public boolean isFrozen() {
        return frozen;
    }

    // internal – used by DevkitRegistry auto-bootstrap
    Map<Identifier, Supplier<? extends T>> getRawEntries() {
        return entries;
    }
}
