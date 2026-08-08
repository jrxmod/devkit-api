package io.github.jrxmod.testmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.jrxmod.devkit.components.Components;
import io.github.jrxmod.devkit.config.ConfigManager;
import io.github.jrxmod.devkit.config.SyncedConfig;
import io.github.jrxmod.devkit.core.DevkitCommands;
import io.github.jrxmod.devkit.networking.AutoPacket;
import io.github.jrxmod.devkit.networking.AutoPacketRegistry;
import io.github.jrxmod.devkit.networking.DevkitNetworking;
import io.github.jrxmod.devkit.networking.NetworkCompat;
import io.github.jrxmod.devkit.networking.SyncedPacket;
import io.github.jrxmod.devkit.registry.ItemSettings;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class TestMod implements ModInitializer {
    public static final String MOD_ID = "devkit_test";
    public static final KRegister<Item> ITEMS = KRegister.create(MOD_ID, RegistryKeys.ITEM);
    public static final RegistrySupplier<Item> RUBY = ITEMS.register("ruby", ItemSettings.of().maxCount(64).buildKeyed());
    public static final ComponentType<Integer> ENERGY = Components.intComponent(MOD_ID, "energy").cache().buildAndRegister();
    public static TestConfig CONFIG;

    @Override
    public void onInitialize() {
        ITEMS.bootstrap(Registries.ITEM);
        CONFIG = ConfigManager.loadOrCreate(TestConfig.class, MOD_ID, "main");
        AutoPacketRegistry.register(EnergySyncPacket.class);
        AutoPacketRegistry.register(SetEnergyPacket.class);
        registerCommands();
    }

    private static void registerCommands() {
        DevkitCommands.register(MOD_ID, root -> {
            root.then(literal("energy").executes(ctx -> {
                ServerPlayerEntity player = DevkitCommands.player(ctx);
                DevkitNetworking.sendToPlayer(player, new EnergySyncPacket(player.getBlockPos(), CONFIG.maxEnergy));
                NetworkCompat.reconfigureIfAvailable(player);
                return DevkitCommands.ok(ctx, "Sent energy sync: " + CONFIG.maxEnergy);
            }));
            root.then(literal("set").then(argument("value", integer(0, 100_000)).executes(ctx -> {
                int value = IntegerArgumentType.getInteger(ctx, "value");
                CONFIG.maxEnergy = value;
                ConfigManager.save(CONFIG, MOD_ID, "main");
                return DevkitCommands.okBroadcast(ctx, "Energy set to " + value);
            })));
            root.then(literal("reload").executes(ctx -> {
                CONFIG = ConfigManager.reload(TestConfig.class, MOD_ID, "main");
                return DevkitCommands.okBroadcast(ctx, "Config reloaded. maxEnergy=" + CONFIG.maxEnergy);
            }));
        });
    }

    @AutoPacket(value = "devkit_test:energy_sync", direction = AutoPacket.Direction.S2C)
    public record EnergySyncPacket(BlockPos pos, int energy) implements SyncedPacket {
        public static final CustomPayload.Id<EnergySyncPacket> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "energy_sync"));
        public static final PacketCodec<RegistryByteBuf, EnergySyncPacket> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, EnergySyncPacket::pos, PacketCodecs.VAR_INT, EnergySyncPacket::energy, EnergySyncPacket::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
        @Override public void handle(NetworkContext context) {
            if (context.isClient()) { context.queue(() -> { TestConfig c = CONFIG; if (c != null) c.maxEnergy = energy; }); }
        }
    }

    @AutoPacket(value = "devkit_test:set_energy", direction = AutoPacket.Direction.C2S)
    public record SetEnergyPacket(int energy) implements SyncedPacket {
        public static final CustomPayload.Id<SetEnergyPacket> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "set_energy"));
        public static final PacketCodec<RegistryByteBuf, SetEnergyPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, SetEnergyPacket::energy, SetEnergyPacket::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
        @Override public void handle(NetworkContext context) {
            if (!context.isClient()) { context.queue(() -> { TestConfig c = CONFIG; if (c != null && energy >= 0 && energy <= 100_000) { c.maxEnergy = energy; ConfigManager.save(c, MOD_ID, "main"); } }); }
        }
    }

    @SyncedConfig(MOD_ID)
    public static final class TestConfig {
        public int maxEnergy = 10_000;
        public boolean enableParticles = true;
        public String welcomeMessage = "DevKit API by jrxmod";
    }
}
