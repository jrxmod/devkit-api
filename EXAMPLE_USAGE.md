# DevKit API — Example Usage

The executable development example lives in:

```text
devkit-testmod-example/src/main/java/io/github/jrxmod/testmod/TestMod.java
```

One source file compiles on all three Minecraft versions — no overrides needed
since `ItemSettings.buildKeyed()` handles the registry-key difference internally.

## Consumer setup

### Modrinth Maven (recommended)

```gradle
repositories {
    maven { url = "https://api.modrinth.com/maven/" }
    maven { url = "https://maven.fabricmc.net/" }
}

dependencies {
    modImplementation "maven.modrinth:devkitapi:0.2.0-alpha.2+1.21.8"
}
```

### Maven Local (fallback)

Publish the target version locally:

```bash
./gradlew publishToMavenLocal
./gradlew -p versions/1.21.8 publishToMavenLocal
./gradlew -p versions/1.21.11 publishToMavenLocal
```

Then depend on it:

```gradle
repositories {
    mavenLocal()
    maven { url = "https://maven.fabricmc.net/" }
}

dependencies {
    modImplementation "io.github.jrxmod.devkit:devkit-fabric:0.2.0-alpha.2+1.21.8"
}
```

## Available versions

```text
0.2.0-alpha.2+1.21.1
0.2.0-alpha.2+1.21.8
0.2.0-alpha.2+1.21.11
```

## New in 0.2.0-alpha.2

- `ItemSettings.of().maxCount(64).buildKeyed()` — version-resilient item registration.
- `BlockSettings.of().strength(5f).buildKeyed()` — same for blocks.
- `DevkitCommands.register("mymod", ...)` — thin Brigadier wrappers.
- `ConfigManager.reload(Class, modId, name)` — reread config without restart.
- ModMenu library badge now displays correctly.

See `README.md` for full quick-start examples.
