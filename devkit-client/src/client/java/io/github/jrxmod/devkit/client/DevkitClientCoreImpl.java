package io.github.jrxmod.devkit.client;

import io.github.jrxmod.devkit.core.DevkitCore;
import io.github.jrxmod.devkit.networking.AutoPacket;
import io.github.jrxmod.devkit.networking.AutoPacketRegistry;
import io.github.jrxmod.devkit.networking.SyncedPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-only implementation of the DevKit client bootstrap.
 * <p>
 * Lives in the {@code src/client/} source set so that
 * {@code ClientPlayNetworking} and {@code MinecraftClient} are only
 * compiled into the client jar. The common-side {@link DevkitClientCore}
 * delegates here via reflection.
 *
 * @author jrxmod
 * @since 0.1.0
 */
@Environment(EnvType.CLIENT)
public final class DevkitClientCoreImpl {
    private static final Logger LOGGER = DevkitCore.LOGGER;
    private static final Set<CustomPayload.Id<?>> CLIENT_REGISTERED = ConcurrentHashMap.newKeySet();

    private DevkitClientCoreImpl() {}

    /**
     * Walks the packet entries registered on the common side during
     * init and wires matching S2C receivers via Fabric's
     * {@code ClientPlayNetworking}.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        LOGGER.info("[DevKit] Client module initialized by jrxmod");
        try {
            int count = 0;
            for (AutoPacketRegistry.PacketEntry<?> entry : AutoPacketRegistry.getEntries()) {
                AutoPacket.Direction dir = entry.direction();
                if (dir == AutoPacket.Direction.S2C || dir == AutoPacket.Direction.BIDIRECTIONAL) {
                    if (registerClientReceiver(entry)) {
                        count++;
                    }
                }
            }
            LOGGER.info("[DevKit] Auto-registered {} client packet receivers", count);
        } catch (Throwable t) {
            LOGGER.debug("Client packet auto-registration skipped: {}", t.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean registerClientReceiver(AutoPacketRegistry.PacketEntry<?> entry) {
        try {
            Field idField = entry.packetClass().getField("ID");
            CustomPayload.Id<?> id = (CustomPayload.Id<?>) idField.get(null);
            if (!CLIENT_REGISTERED.add(id)) {
                return false;
            }
            ClientPlayNetworking.registerGlobalReceiver((CustomPayload.Id) id, (payload, context) -> {
                if (payload instanceof SyncedPacket synced) {
                    synced.handle(new ClientNetworkContext(context.client().player));
                }
            });
            return true;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to register client receiver for {}", entry.packetClass().getName(), e);
            return false;
        }
    }

    private record ClientNetworkContext(PlayerEntity player) implements SyncedPacket.NetworkContext {
        @Override public boolean isClient() { return true; }
        @Override public PlayerEntity getPlayer() { return player; }
        @Override public void queue(Runnable task) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) client.execute(task);
        }
    }
}
