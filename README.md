# DevKit API
**by jrxmod**

Modular Fabric API library — Minecraft 1.21.1 LTS & 1.21.8 / 1.21.11

Apache-2.0 • Java 21 • Fabric Loader ≥0.19.3

---

## 🇷🇺 Что это?

**DevKit API** — это библиотека-помощник для разработчиков Fabric-модов. Сама по себе она не добавляет контент в игру. Её ставят как зависимость другие моды.

Помогает решить 4 главные боли 1.21.x:

1. **Registry Kit** — регистрация блоков/предметов/энтити в 2 строки через `KRegister`
2. **Auto-Networking** — `@AutoPacket` → автоматическая регистрация Payload S2C/C2S, без ручного ByteBuf
3. **Data Components 1.21+** — fluent билдер для `DataComponentType`, авто-sync
4. **Config Sync** — `@SyncedConfig`, сервер-авторитативный конфиг, авто S2C

Размер jar: **<350 KB**, zero внешних зависимостей (только Fabric API).

## 🇬🇧 What is it?

**DevKit API** is a lightweight developer toolkit for Fabric 1.21.x modders.

- Fluent Registry Kit (`KRegister`)
- Auto-Networking (`@AutoPacket`)
- Data Components helpers for 1.21+
- Server-authoritative Synced Config

No gameplay content — pure library.

---

## Modules / Модули

```
io.github.jrxmod.devkit:devkit-core:0.1.0+1.21.1
io.github.jrxmod.devkit:devkit-registry
io.github.jrxmod.devkit:devkit-networking
io.github.jrxmod.devkit:devkit-components
io.github.jrxmod.devkit:devkit-config
io.github.jrxmod.devkit:devkit-client
io.github.jrxmod.devkit:devkit-datagen
io.github.jrxmod.devkit:devkit-fabric   // all-in-one
```

### Gradle
```gradle
repositories {
    maven { url "https://maven.modrinth.com" }
}
dependencies {
    modImplementation "io.github.jrxmod.devkit:devkit-fabric:0.1.0+1.21.1"
}
```

## Quick start / Быстрый старт

```java
// Registry
public class MyItems {
  public static final KRegister<Item> ITEMS = KRegister.create("mymod", Registries.ITEM);
  public static final RegistrySupplier<Item> RUBY = ITEMS.register("ruby",
    () -> new Item(new Item.Settings()));
}

// Data Component 1.21+
public static final DataComponentType<Integer> CHARGE =
  Components.intComponent("charge").persistent().build();

// Auto packet
@AutoPacket(value = "sync_charge", direction = AutoPacket.Direction.S2C)
public record ChargeSyncPacket(BlockPos pos, int charge) implements SyncedPacket {}

// Config
@SyncedConfig("mymod")
public class MyConfig {
  public int maxCharge = 100;
}
```

## Версии / Versions

- **1.21.1 LTS** — Fabric API 0.116.13+1.21.1 — основная ветка
- **1.21.8** — Fabric API 0.136.1+1.21.8
- **1.21.11** — Fabric API 0.141.4+1.21.11

Dual-branch релизы, semver: `0.1.0+1.21.1`

## Автор / Author
- **jrxmod**
- GitHub: https://github.com/jrxmod/devkit-api
- Modrinth: https://modrinth.com/mod/devkit-api
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/devkit-api
- License: Apache-2.0
