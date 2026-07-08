package io.github.jrxmod.devkit.registry;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry Kit bootstrap manager.
 * <p>
 * Tracks all {@link KRegister} instances created by dependent mods
 * and provides lifecycle hooks for automatic registration where possible.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class DevkitRegistry {
    private static final Logger LOGGER = DevkitCore.LOGGER;
    private static final List<KRegister<?>> TRACKED = new ArrayList<>();
    private static boolean initialized = false;

    private DevkitRegistry() {}

    /**
     * Initializes the registry subsystem.
     * Called automatically by DevKit core.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("[DevKit] Registry Kit ready ({} tracked registers)", TRACKED.size());
    }

    /**
     * Internal tracking hook invoked by {@link KRegister} constructor.
     *
     * @param register register instance
     * @param <T> value type
     */
    static <T> void track(KRegister<T> register) {
        TRACKED.add(register);
        LOGGER.debug("Tracking KRegister: {} -> {}", register.getModId(), register.getRegistryKey().getValue());
    }

    /**
     * Returns an immutable snapshot of tracked registers.
     * Useful for diagnostics and test harnesses.
     *
     * @return tracked registers
     */
    public static List<KRegister<?>> getTrackedRegisters() {
        return List.copyOf(TRACKED);
    }

    /**
     * Attempts best-effort auto-bootstrap for all tracked registers
     * using vanilla {@link Registries} ROOT lookups.
     * <p>
     * Dependent mods may still invoke {@code KRegister.bootstrap()} manually
     * for precise ordering control.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void bootstrapAll() {
        int bootstrapped = 0;
        for (KRegister<?> r : TRACKED) {
            if (r.isFrozen()) continue;
            Registry<?> target = Registries.REGISTRIES.get(r.getRegistryKey().getValue());
            if (target != null) {
                ((KRegister) r).bootstrap((Registry) target);
                bootstrapped++;
            }
        }
        if (bootstrapped > 0) {
            LOGGER.info("[DevKit] Auto-bootstrapped {} registry containers", bootstrapped);
        }
    }
}
