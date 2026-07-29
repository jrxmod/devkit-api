# DevKit API

**DevKit API** by **jrxmod** is a small developer library for Fabric mods. It does not add gameplay content by itself.

> Current development release: `0.2.0-alpha.1` for Minecraft 1.21.x.
>
> Java 21 · Fabric Loader 0.19.3 · Apache-2.0

## Supported Minecraft versions

DevKit is built as three separate JARs from one `main` branch:

| Minecraft | Artifact |
|---|---|
| 1.21.1 LTS | `devkit-api-0.2.0-alpha.1+1.21.1.jar` |
| 1.21.8 | `devkit-api-0.2.0-alpha.1+1.21.8.jar` |
| 1.21.11 | `devkit-api-0.2.0-alpha.1+1.21.11.jar` |

A player or dependent mod should use exactly the JAR matching its Minecraft version. There is no 26.x target planned.

## Features

- **Registry Kit** — ordered typed registration through `KRegister`.
- **Payload networking** — validated S2C/C2S packet registration and send helpers.
- **Data Components** — fluent `ComponentType` builder for Minecraft 1.21+.
- **Synced JSON config** — optional server-authoritative config synchronization.
- **Datagen helpers** — base providers for models, tags and block loot tables.

The API is alpha software. Public signatures may still change before 1.0.

## Development dependency

Until a Maven repository is announced, publish the required version locally:

```bash
# Minecraft 1.21.1
./gradlew publishToMavenLocal

# Minecraft 1.21.8
./gradlew -p versions/1.21.8 publishToMavenLocal

# Minecraft 1.21.11
./gradlew -p versions/1.21.11 publishToMavenLocal
```

Then select the matching version in a dependent mod:

```gradle
repositories {
    mavenLocal()
    maven { url = "https://maven.fabricmc.net/" }
}

dependencies {
    modImplementation "io.github.jrxmod.devkit:devkit-fabric:0.2.0-alpha.1+1.21.8"
}
```

For an all-in-one runtime installation, use the `devkit-api` JAR produced by the `devkit-fabric` module.

## Quick start

### Registry

Create the registry container with the key from `RegistryKeys`:

```java
public static final KRegister<Item> ITEMS =
        KRegister.create("mymod", RegistryKeys.ITEM);
```

Minecraft 1.21.1 accepts the simple supplier form:

```java
public static final RegistrySupplier<Item> RUBY =
        ITEMS.register("ruby", () -> new Item(new Item.Settings()));
```

Minecraft 1.21.8 and 1.21.11 require the registry key in item and block settings. Use the key-aware factory form:

```java
public static final RegistrySupplier<Item> RUBY =
        ITEMS.register("ruby", key ->
                new Item(new Item.Settings().registryKey(key)));
```

Bootstrap from the owning mod initializer:

```java
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

Build one version:

```bash
./gradlew clean build
./gradlew -p versions/1.21.8 clean build
./gradlew -p versions/1.21.11 clean build
```

Build all versions and collect the all-in-one artifacts:

```bash
./gradlew collectReleaseJars
ls -lh build/releases
```

Version-specific source replacements live below `versions/<minecraft>/overrides`. Everything else is compiled from the shared module source tree, so fixes do not have to be copied between Git branches.

The development integration mod is included as `devkit-testmod-example` and is compiled for every supported version.

## License

Copyright 2026 jrxmod. Licensed under Apache-2.0.
