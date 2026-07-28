# DevKit API — Example Usage

The executable development example lives in:

```text
devkit-testmod-example/src/main/java/io/github/jrxmod/testmod/TestMod.java
```

It is included in `settings.gradle`, so API examples are compiled by the normal build instead of silently becoming outdated.

## Consumer setup during alpha development

Publish DevKit to the local Maven repository:

```bash
./gradlew publishToMavenLocal
```

Then add:

```gradle
repositories {
    mavenLocal()
    maven { url = "https://maven.fabricmc.net/" }
}

dependencies {
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.116.13+1.21.1"
    modImplementation "io.github.jrxmod.devkit:devkit-fabric:0.2.0-alpha.1+1.21.1"
}
```

See `README.md` for focused registry, component, networking and config examples.
