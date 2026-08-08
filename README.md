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

- **Registry Kit** — ordered typed registration through `KRegister`, plus version-resilient `ItemSettings` / `BlockSettings` builders.
- **Payload networking** — validated S2C/C2S packet registration and send helpers.
- **Data Components** — fluent `ComponentType` builder for Minecraft 1.21+.
- **Synced JSON config** — optional server-authoritative config synchronization.
- **Command helpers** — thin Brigadier wrappers: auto-registration, feedback shortcuts, player unwrap.
- **Datagen helpers** — base providers for models, tags and block loot tables.
The API is alpha software. Public signatures may still change before 1.0.

## Development dependency

### Modrinth Maven (recommended)

DevKit API is published on Modrinth. Add the Modrinth Maven repository and
select the artifact matching your target Minecraft version:

```gradle
repositories {
    maven { url = "https://api.modrinth.com/maven/" }
    maven { url = "https://maven.fabricmc.net/" }
}

dependencies {
    // Minecraft 1.21.1
    modImplementation "maven.modrinth:devkitapi:0.2.0-alpha.1+1.21.1"
    // Minecraft 1.21.8
    modImplementation "maven.modrinth:devkitapi:0.2.0-alpha.1+1.21.8"
    // Minecraft 1.21.11
    modImplementation "maven.modrinth:devkitapi:0.2.0-alpha.1+1.21.11"
}
```

The Modrinth artifact is the all-in-one `devkit-api` JAR. Pick exactly one line
for your Minecraft version.

### Maven Local (fallback)

If a version has not yet been published on Modrinth, publish it locally first:

```bash
# Minecraft 1.21.1
./gradlew publishToMavenLocal

# Minecraft 1.21.8
./gradlew -p versions/1.21.8 publishToMavenLocal

# Minecraft 1.21.11
./gradlew -p versions/1.21.11 publishToMavenLocal
```

Then depend on it:

```gradle
repositories {
    mavenLocal()
    maven { url = "https://maven.fabricmc.net/" }
}

dependencies {
    modImplementation "io.github.jrxmod.devkit:devkit-fabric:0.2.0-alpha.1+1.21.8"
}
```

The Maven Local coordinate uses the `devkit-fabric` module, which produces the
same all-in-one JAR.

## Quick start

### Registry

Create the registry container with the key from `RegistryKeys`:

```java
public static final KRegister<Item> ITEMS =
        KRegister.create("mymod", RegistryKeys.ITEM);
```

Use the version-resilient `ItemSettings` builder — the same code works on
all supported Minecraft versions:

```java
public static final RegistrySupplier<Item> RUBY =
        ITEMS.register("ruby", ItemSettings.of().maxCount(64).buildKeyed());
```

For blocks:

```java
public static final KRegister<Block> BLOCKS =
        KRegister.create("mymod", RegistryKeys.BLOCK);

public static final RegistrySupplier<Block> RUBY_BLOCK =
        BLOCKS.register("ruby_block", BlockSettings.of().strength(5f).buildKeyed());
```

Under the hood, `buildKeyed()` produces a key-aware factory. On 1.21.1 the
key is ignored; on 1.21.8+ it is applied via `.registryKey(key)`. Both
`buildSimple()` (supplier) and `buildKeyed()` (function) are available.

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
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}

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

The synchronization key is `mymod:main`. To resend after a reload:

```java
ConfigSyncManager.broadcastToAll(server, "mymod:main");
```

### Commands

DevKit provides thin helpers over Brigadier to cut down boilerplate:

```java
DevkitCommands.register("mymod", root -> {
    root.then(literal("heal").executes(ctx -> {
        DevkitCommands.player(ctx).setHealth(20);
        return DevkitCommands.ok(ctx, "Healed!");
    }));

    root.then(literal("reload").executes(ctx -> {
        // reload config ...
        return DevkitCommands.okBroadcast(ctx, "Reloaded");
    }));
});
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
