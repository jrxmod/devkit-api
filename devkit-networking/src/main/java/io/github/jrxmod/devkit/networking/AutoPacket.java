package io.github.jrxmod.devkit.networking;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the identifier and direction of a {@link SyncedPacket} class.
 * Registration is explicit through {@link AutoPacketRegistry#register(Class)};
 * the annotation supplies validated metadata rather than scanning the classpath.
 *
 * @author jrxmod
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoPacket {
    /** @return payload identifier in {@code namespace:path} form */
    String value();

    /** @return allowed network direction */
    Direction direction() default Direction.BIDIRECTIONAL;

    enum Direction {
        S2C(true, false),
        C2S(false, true),
        BIDIRECTIONAL(true, true);

        private final boolean serverToClient;
        private final boolean clientToServer;

        Direction(boolean serverToClient, boolean clientToServer) {
            this.serverToClient = serverToClient;
            this.clientToServer = clientToServer;
        }

        public boolean allowsServerToClient() {
            return serverToClient;
        }

        public boolean allowsClientToServer() {
            return clientToServer;
        }
    }
}
