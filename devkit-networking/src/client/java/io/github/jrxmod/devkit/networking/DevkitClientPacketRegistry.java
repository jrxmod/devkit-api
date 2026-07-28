package io.github.jrxmod.devkit.networking;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Client-only receiver wiring for packets declared on the common side. */
@Environment(EnvType.CLIENT)
public final class DevkitClientPacketRegistry {
    private static final Set<CustomPayload.Id<?>> REGISTERED = ConcurrentHashMap.newKeySet();
    private static volatile boolean initialized;

    private DevkitClientPacketRegistry() {}

    /** Registers existing S2C entries and observes registrations made later. */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        int count = 0;
        for (AutoPacketRegistry.PacketEntry<?> entry : AutoPacketRegistry.getEntries()) {
            if (registerIfClientbound(entry)) {
                count++;
            }
        }
        AutoPacketRegistry.addRegistrationListener(DevkitClientPacketRegistry::registerIfClientbound);
        DevkitCore.LOGGER.info("[DevKit] Registered {} client packet receivers", count);
    }

    private static boolean registerIfClientbound(AutoPacketRegistry.PacketEntry<?> entry) {
        if (!entry.direction().allowsServerToClient()) {
            return false;
        }
        return register(entry);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean register(AutoPacketRegistry.PacketEntry<?> entry) {
        CustomPayload.Id<?> id = entry.id();
        if (!REGISTERED.add(id)) {
            return false;
        }
        try {
            boolean registered = ClientPlayNetworking.registerGlobalReceiver((CustomPayload.Id) id,
                    (payload, context) -> ((SyncedPacket) payload)
                            .handle(new ClientNetworkContext(context.player(), context.client())));
            if (!registered) {
                REGISTERED.remove(id);
                DevkitCore.LOGGER.warn("A client receiver is already registered for {}", id.id());
                return false;
            }
            return true;
        } catch (RuntimeException e) {
            REGISTERED.remove(id);
            throw e;
        }
    }

    private record ClientNetworkContext(
            PlayerEntity player,
            net.minecraft.client.MinecraftClient client
    ) implements SyncedPacket.NetworkContext {
        @Override public boolean isClient() { return true; }
        @Override public PlayerEntity getPlayer() { return player; }
        @Override public void queue(Runnable task) { client.execute(task); }
    }
}
