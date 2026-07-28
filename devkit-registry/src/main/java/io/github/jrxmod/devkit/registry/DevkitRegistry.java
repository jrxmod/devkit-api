package io.github.jrxmod.devkit.registry;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Tracks DevKit registry containers and supports explicit bulk bootstrap. */
public final class DevkitRegistry {
    private static final List<KRegister<?>> TRACKED = new CopyOnWriteArrayList<>();
    private static volatile boolean initialized;

    private DevkitRegistry() {}

    public static void init() {
        if (!initialized) {
            initialized = true;
            DevkitCore.LOGGER.info("[DevKit] Registry Kit initialized");
        }
    }

    static <T> void track(KRegister<T> register) {
        TRACKED.add(register);
    }

    public static List<KRegister<?>> getTrackedRegisters() {
        return List.copyOf(TRACKED);
    }

    /**
     * Bootstraps every currently tracked container. Call this from the owning
     * mod initializer after its static registration declarations have loaded.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void bootstrapAll() {
        int count = 0;
        for (KRegister<?> register : TRACKED) {
            if (register.isFrozen()) {
                continue;
            }
            Registry<?> target = Registries.REGISTRIES.get(register.getRegistryKey().getValue());
            if (target == null) {
                throw new IllegalStateException("Unknown registry: " + register.getRegistryKey().getValue());
            }
            ((KRegister) register).bootstrap((Registry) target);
            count++;
        }
        DevkitCore.LOGGER.debug("Bootstrapped {} registry containers", count);
    }
}
