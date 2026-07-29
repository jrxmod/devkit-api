package io.github.jrxmod.testmod;

import io.github.jrxmod.devkit.components.Components;
import io.github.jrxmod.devkit.config.ConfigManager;
import io.github.jrxmod.devkit.config.SyncedConfig;
import io.github.jrxmod.devkit.networking.AutoPacket;
import io.github.jrxmod.devkit.networking.AutoPacketRegistry;
import io.github.jrxmod.devkit.networking.SyncedPacket;
import io.github.jrxmod.devkit.registry.KRegister;
import io.github.jrxmod.devkit.registry.RegistrySupplier;
import net.fabricmc.api.ModInitializer;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** Development integration mod for registry-key-aware 1.21.x versions. */
public final class TestMod implements ModInitializer {
    public static final String MOD_ID = "devkit_test";

    public static final KRegister<Item> ITEMS = KRegister.create(MOD_ID, RegistryKeys.ITEM);
    public static final RegistrySupplier<Item> RUBY = ITEMS.register("ruby", key ->
            new Item(new Item.Settings().registryKey(key).maxCount(64)));

    public static final ComponentType<Integer> ENERGY = Components.intComponent(MOD_ID, "energy")
            .cache()
            .buildAndRegister();

    public static TestConfig CONFIG;

    @Override
    public void onInitialize() {
        ITEMS.bootstrap(Registries.ITEM);
        CONFIG = ConfigManager.loadOrCreate(TestConfig.class, MOD_ID, "main");
        AutoPacketRegistry.register(EnergySyncPacket.class);
        AutoPacketRegistry.register(SetEnergyPacket.class);
    }

    @AutoPacket(value = "devkit_test:energy_sync", direction = AutoPacket.Direction.S2C)
    public record EnergySyncPacket(BlockPos pos, int energy) implements SyncedPacket {
        public static final CustomPayload.Id<EnergySyncPacket> ID =
                new CustomPayload.Id<>(Identifier.of(MOD_ID, "energy_sync"));
        public static final PacketCodec<RegistryByteBuf, EnergySyncPacket> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, EnergySyncPacket::pos,
                PacketCodecs.VAR_INT, EnergySyncPacket::energy,
                EnergySyncPacket::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        @Override
        public void handle(NetworkContext context) {
            context.queue(() -> {
                // A real dependent mod would update client state here.
            });
        }
    }

    @AutoPacket(value = "devkit_test:set_energy", direction = AutoPacket.Direction.C2S)
    public record SetEnergyPacket(int energy) implements SyncedPacket {
        public static final CustomPayload.Id<SetEnergyPacket> ID =
                new CustomPayload.Id<>(Identifier.of(MOD_ID, "set_energy"));
        public static final PacketCodec<RegistryByteBuf, SetEnergyPacket> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, SetEnergyPacket::energy,
                SetEnergyPacket::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }

        @Override
        public void handle(NetworkContext context) {
            if (!context.isClient()) {
                context.queue(() -> {
                    // Validate permissions and bounds before changing server state.
                });
            }
        }
    }

    @SyncedConfig(MOD_ID)
    public static final class TestConfig {
        public int maxEnergy = 10_000;
        public boolean enableParticles = true;
        public String welcomeMessage = "DevKit API by jrxmod";
    }
}
