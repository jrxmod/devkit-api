package io.github.jrxmod.devkit.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JSON configuration class as eligible for server-to-client sync.
 *
 * <p>The value is the owning mod namespace. The file name supplied to
 * {@link ConfigManager#loadOrCreate(Class, String, String)} completes the
 * unique synchronization key, for example {@code mymod:main}.</p>
 *
 * @author jrxmod
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SyncedConfig {
    /** @return owning mod namespace */
    String value();

    /**
     * Determines whether values from a server replace the local client's
     * values. Non-authoritative configs are loaded normally but are not sent.
     *
     * @return {@code true} to synchronize server values to clients
     */
    boolean serverAuthoritative() default true;
}
