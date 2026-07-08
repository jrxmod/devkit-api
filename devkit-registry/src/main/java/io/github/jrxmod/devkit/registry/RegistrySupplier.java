package io.github.jrxmod.devkit.registry;

import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Type-safe lazy holder for registry entries created via {@link KRegister}.
 * <p>
 * The underlying value is bound during registry bootstrap and remains
 * constant afterwards.
 *
 * @param <T> registry value type
 * @author jrxmod
 * @since 0.1.0
 */
public class RegistrySupplier<T> implements Supplier<T> {
    private final Identifier id;
    private volatile T value;

    /**
     * Creates a new unbound holder.
     *
     * @param id registry identifier
     */
    public RegistrySupplier(Identifier id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    /**
     * Internal binding invoked by {@link KRegister} during bootstrap.
     *
     * @param value resolved registry object
     */
    void bind(T value) {
        this.value = value;
    }

    /**
     * Returns the bound registry object.
     *
     * @return registry value
     * @throws IllegalStateException if accessed before bootstrap
     */
    @Override
    public T get() {
        if (value == null) {
            throw new IllegalStateException("Registry object " + id + " accessed before bootstrap – ensure KRegister.bootstrap() has run");
        }
        return value;
    }

    /**
     * @return registry identifier
     */
    public Identifier getId() {
        return id;
    }

    /**
     * @return true if the holder has been bound
     */
    public boolean isPresent() {
        return value != null;
    }

    @Override
    public String toString() {
        return "RegistrySupplier[" + id + (value != null ? "=" + value.getClass().getSimpleName() : ", unbound") + "]";
    }
}
