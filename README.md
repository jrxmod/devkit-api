# DevKit API

**DevKit API** by **jrxmod** is a small developer library for Fabric mods. It does not add gameplay content by itself.

> Current development release: `0.2.0-alpha.1` for Minecraft 1.21.1.
>
> Java 21 · Fabric Loader 0.19.3 · Apache-2.0

## Features

- **Registry Kit** — ordered typed registration through `KRegister`.
- **Payload networking** — validated S2C/C2S packet registration and send helpers.
- **Data Components** — fluent `ComponentType` builder for Minecraft 1.21+.
- **Synced JSON config** — optional server-authoritative config synchronization.
- **Datagen helpers** — base providers for models, tags and block loot tables.

The API is alpha software. Public signatures may still change before 1.0.

## Development dependency

Until a Maven repository is announced, use a local Maven publication:

```bash
./gradlew publishToMavenLocal
```

```gradle
repositories {
    mavenLocal()
    maven { url = "https://maven.fabricmc.net/" }
}

dependencies {
    modImplementation "io.github.jrxmod.devkit:devkit-fabric:0.2.0-alpha.1+1.21.1"
}
```

For an all-in-one runtime installation, use the `devkit-api` JAR produced by the `devkit-fabric` module.

## Quick start

### Registry

```java
public static final KRegister<Item> ITEMS =
        KRegister.create("mymod", RegistryKeys.ITEM);

public static final RegistrySupplier<Item> RUBY =
        ITEMS.register("ruby", () -> new Item(new Item.Settings()));

@Override
public void onInitialize() {
    ITEMS.bootstrap(Registries.ITEM);
}
```

Do not access `RUBY.get()` before `bootstrap` completes.

### Data Component

```java
public static final ComponentType<Integer> ENERGY =
        Components.intComponent("mymod", "energy")
                .cache()
                .buildAndRegister();
```

### Network payload

```java
@AutoPacket(value = "mymod:energy_sync", direction = AutoPacket.Direction.S2C)
public record EnergySync(int energy) implements SyncedPacket {
    public static final CustomPayload.Id<EnergySync> ID =
            new CustomPayload.Id<>(Identifier.of("mymod", "energy_sync"));

    public static final PacketCodec<RegistryByteBuf, EnergySync> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, EnergySync::energy, EnergySync::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

// Common initialization, before a connection is established:
AutoPacketRegistry.register(EnergySync.class);
```

Server send:

```java
DevkitNetworking.sendToPlayer(player, new EnergySync(100));
```

Client-to-server payloads are sent from client-only code through `DevkitClientNetworking`.

### Synced config

```java
@SyncedConfig("mymod")
public final class MyConfig {
    public int maxEnergy = 10_000;
}

MyConfig config = ConfigManager.loadOrCreate(MyConfig.class, "mymod", "main");
```

The synchronization key in this example is `mymod:main`. To resend after a reload:

```java
ConfigSyncManager.broadcastToAll(server, "mymod:main");
```

## Build

```bash
./gradlew clean build
```

The development integration mod is included as `devkit-testmod-example` and is compiled with the main project.

## Version plan

1. Stabilize and test Minecraft 1.21.1.
2. Add a separate 1.21.11 build.
3. Port to Minecraft 26.2 / Java 25 using the unobfuscated Fabric toolchain.

Each Minecraft line will receive its own JAR; the project will remain in a single Git branch.

## License

Copyright 2026 jrxmod. Licensed under Apache-2.0.
