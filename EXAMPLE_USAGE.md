# DevKit API — Example Usage

The executable development example lives in:

```text
devkit-testmod-example/src/main/java/io/github/jrxmod/testmod/TestMod.java
```

For Minecraft 1.21.8 and 1.21.11, the registry-key-aware variant is under each version's `overrides` directory. Every example is compiled by its normal version build.

## Consumer setup during alpha development

Publish the target version to the local Maven repository:

```bash
./gradlew publishToMavenLocal
./gradlew -p versions/1.21.8 publishToMavenLocal
./gradlew -p versions/1.21.11 publishToMavenLocal
```

Then add the coordinates matching the consumer's Minecraft version. For example, Minecraft 1.21.8:

```gradle
repositories {
    mavenLocal()
    maven { url = "https://maven.fabricmc.net/" }
}

dependencies {
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.136.1+1.21.8"
    modImplementation "io.github.jrxmod.devkit:devkit-fabric:0.2.0-alpha.1+1.21.8"
}
```

Available DevKit versions:

```text
0.2.0-alpha.1+1.21.1
0.2.0-alpha.1+1.21.8
0.2.0-alpha.1+1.21.11
```

See `README.md` for focused registry, component, networking and config examples.
