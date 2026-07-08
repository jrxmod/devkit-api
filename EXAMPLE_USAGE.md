# DevKit API — Example Usage
**Author: jrxmod**

## Gradle (consumer)
```gradle
repositories {
    maven { url "https://maven.modrinth.com" }
}
dependencies {
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.116.13+1.21.1"
    modImplementation "io.github.jrxmod.devkit:devkit-fabric:0.1.0+1.21.1"
}
```

## 1. Registry Kit
```java
import io.github.jrxmod.devkit.registry.KRegister;
import io.github.jrxmod.devkit.registry.RegistrySupplier;

public class MyItems {
    public static final KRegister<Item> ITEMS =
        KRegister.create("mymod", Registries.ITEM);

    public static final RegistrySupplier<Item> RUBY =
        ITEMS.register("ruby", () -> new Item(new Item.Settings()));
}

// ModInitializer:
@Override
public void onInitialize() {
    ITEMS.bootstrap(Registries.ITEM);
    // or rely on DevkitRegistry.bootstrapAll()
}
```

## 2. Data Components 1.21+
```java
import io.github.jrxmod.devkit.components.Components;

public class MyComponents {
    public static final DataComponentType<Integer> ENERGY =
        Components.builder("mymod", "energy", Codec.INT)
            .networked(PacketCodecs.VAR_INT)
            .cache()
            .buildAndRegister();
}
```

## 3. Auto-Networking
```java
import io.github.jrxmod.devkit.networking.*;

@AutoPacket(value = "mymod/energy_sync", direction = AutoPacket.Direction.S2C)
public record EnergySyncS2C(BlockPos pos, int energy) implements SyncedPacket {
    public static final CustomPayload.Id<EnergySyncS2C> ID =
        new CustomPayload.Id<>(Identifier.of("mymod", "energy_sync"));

    public static final PacketCodec<RegistryByteBuf, EnergySyncS2C> CODEC =
        PacketCodec.tuple(
            BlockPos.PACKET_CODEC, EnergySyncS2C::pos,
            PacketCodecs.VAR_INT, EnergySyncS2C::energy,
            EnergySyncS2C::new
        );

    @Override public Id<? extends CustomPayload> getId() { return ID; }

    @Override public void handle(NetworkContext ctx) {
        ctx.queue(() -> {
            // client handling
        });
    }
}

// Register:
AutoPacketRegistry.register(EnergySyncS2C.class);
AutoPacketRegistry.registerClient(EnergySyncS2C.class); // client init

// Send:
DevkitNetworking.sendToTracking((ServerWorld)world, pos,
    new EnergySyncS2C(pos, energy));
```

## 4. Synced Config
```java
import io.github.jrxmod.devkit.config.*;

@SyncedConfig("mymod")
public class MyConfig {
    public int maxEnergy = 10000;
    public boolean enableParticles = true;
}

// Load:
MyConfig cfg = ConfigManager.loadOrCreate(MyConfig.class, "mymod", "main");
// automatically registered for S2C sync if @SyncedConfig present
```

Full working example: see
`devkit-testmod-example/src/main/java/io/github/jrxmod/testmod/TestMod.java`
