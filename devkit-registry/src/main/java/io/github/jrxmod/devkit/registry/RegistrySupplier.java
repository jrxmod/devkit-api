package io.github.jrxmod.devkit.registry;

import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/** Lazy typed reference bound once by {@link KRegister#bootstrap}. */
public final class RegistrySupplier<T> implements Supplier<T> {
    private final Identifier id;
    private volatile T value;

    RegistrySupplier(Identifier id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    synchronized void bind(T value) {
        Objects.requireNonNull(value, "value");
        if (this.value != null && this.value != value) {
            throw new IllegalStateException("Registry object " + id + " is already bound");
        }
        this.value = value;
    }

    @Override
    public T get() {
        T current = value;
        if (current == null) {
            throw new IllegalStateException("Registry object " + id
                    + " was accessed before KRegister.bootstrap()");
        }
        return current;
    }

    public Optional<T> optional() {
        return Optional.ofNullable(value);
    }

    public Identifier getId() {
        return id;
    }

    public boolean isPresent() {
        return value != null;
    }

    @Override
    public String toString() {
        return "RegistrySupplier[" + id + (value == null ? ", unbound" : ", bound") + ']';
    }
}
