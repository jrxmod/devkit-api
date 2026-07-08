package io.github.jrxmod.devkit.networking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link SyncedPacket} record for automatic registration.
 * <p>
 * DevKit scans annotated types at mod initialization and registers
 * codecs with {@code PayloadTypeRegistry}, as well as global receivers
 * for both S2C and C2S directions as configured.
 * <p>
 * The annotated class must implement {@link SyncedPacket} and expose
 * public static fields {@code ID} and {@code CODEC}.
 *
 * @author jrxmod
 * @since 0.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoPacket {
    /**
     * Channel identifier in {@code namespace:path} form,
     * or a simple path which will be prefixed with the calling mod ID.
     *
     * @return packet channel
     */
    String value();

    /**
     * Network direction for automatic receiver registration.
     *
     * @return allowed direction, defaults to bidirectional
     */
    Direction direction() default Direction.BIDIRECTIONAL;

    enum Direction {
        /** Server to client only */
        S2C,
        /** Client to server only */
        C2S,
        /** Both directions */
        BIDIRECTIONAL
    }
}
