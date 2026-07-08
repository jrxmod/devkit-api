package io.github.jrxmod.testmod;

import io.github.jrxmod.devkit.config.ConfigManager;
import io.github.jrxmod.devkit.config.SyncedConfig;
import io.github.jrxmod.devkit.networking.AutoPacket;
import io.github.jrxmod.devkit.networking.AutoPacketRegistry;
import io.github.jrxmod.devkit.networking.SyncedPacket;
import io.github.jrxmod.devkit.registry.KRegister;
import io.github.jrxmod.devkit.registry.RegistrySupplier;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * DevKit API usage example.
 * Demonstrates Registry Kit, Data Components, Auto-Networking, and Synced Config.
 *
 * @author jrxmod
 */
public class TestMod implements ModInitializer {
    public static final String MOD_ID = "devkit_test";

    // 1. Registry Kit
    public static final KRegister<Item> ITEMS = KRegister.create(MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<Item> RUBY = ITEMS.register("ruby",
            () -> new Item(new Item.Settings().maxCount(64)));

    // 2. Config
    public static TestConfig CONFIG;

    @Override
    public void onInitialize() {
        // Registry bootstrap – can be manual or rely on DevkitRegistry.bootstrapAll()
        ITEMS.bootstrap(Registries.ITEM);

        // Config load with auto-sync
        CONFIG = ConfigManager.loadOrCreate(TestConfig.class, MOD_ID, "main");

        // Networking – register packet
        AutoPacketRegistry.register(EnergySyncPacket.class);
    }

    // 3. Auto-Networking example
    @AutoPacket(value = "devkit_test/energy_sync", direction = AutoPacket.Direction.S2C)
    public record EnergySyncPacket(BlockPos pos, int energy) implements SyncedPacket {
        public static final CustomPayload.Id<EnergySyncPacket> ID =
                new CustomPayload.Id<>(Identifier.of(MOD_ID, "energy_sync"));

        public static final PacketCodec<RegistryByteBuf, EnergySyncPacket> CODEC =
                PacketCodec.tuple(
                        BlockPos.PACKET_CODEC, EnergySyncPacket::pos,
                        PacketCodecs.VAR_INT, EnergySyncPacket::energy,
                        EnergySyncPacket::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        @Override
        public void handle(NetworkContext ctx) {
            ctx.queue(() -> {
                // Client-side handling logic
            });
        }
    }

    // 4. Synced config example
    @SyncedConfig(value = "devkit_test", serverAuthoritative = true)
    public static class TestConfig {
        public int maxEnergy = 10000;
        public boolean enableParticles = true;
        public String welcomeMessage = "DevKit API by jrxmod";
    }
}
