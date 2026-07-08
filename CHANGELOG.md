# Changelog
All notable changes to DevKit API by jrxmod will be documented here.

## [0.1.0-alpha.2] - 2026-07-08
### Fixed
- `ConfigSyncManager.ConfigSyncPacket.handle()` now reflectively copies fields
  from the freshly deserialized JSON into the live config instance, preserving
  reference identity for dependent code (previously a no-op getter).
- Added `ConfigSyncManager.broadcastToAll(server, modId)` for `/reload`-style flows.
- `DevkitNetworking.sendToServer` was moved to a client-only source set
  (`DevkitClientNetworking`) to prevent `NoClassDefFoundError` on
  dedicated servers loading `ClientPlayNetworking`.
- `AutoPacketRegistry` no longer references `ClientPlayNetworking` from
  common code; client registration lives in
  `DevkitClientPacketRegistry` (client source set).
- Removed unused imports (`FabricRegistryBuilder`, `RegistryAttribute`,
  `RegistryEntryAddedCallback`, `SimpleRegistry`).

### Added
- `AutoPacketRegistry.PacketEntry` is now `public` so client-side iteration
  can wire S2C receivers without forcing a common dependency.
- `DevkitDataGenerator` now logs entrypoint readiness for clearer build
  diagnostics and provides documentation on how dependent mods can register
  their own providers.
- `FIX_PLAN.md` at the project root tracking the in-flight fixes.

## [0.1.0-alpha] - 2026-07-08
Minecraft: 1.21.1 / 1.21.8 / 1.21.11  
Fabric Loader: 0.19.3  
Fabric API: 0.116.13+1.21.1 / 0.136.1+1.21.8 / 0.141.4+1.21.11  
Java: 21  
License: Apache-2.0  
Author: jrxmod

### Added
- **devkit-core** – Core API bootstrap, version 1
  - `DevkitCore` central logger and init gate
- **devkit-registry** – Fluent Registry Kit
  - `KRegister<T>` – type-safe registry container with duplicate detection
  - `RegistrySupplier<T>` – lazy holder with clear error on pre-bootstrap access
  - `DevkitRegistry` – global tracking + `bootstrapAll()` auto-bootstrap
- **devkit-networking** – Auto-Networking (Payload API)
  - `SyncedPacket extends CustomPayload` – base packet interface
  - `@AutoPacket` – annotation-driven registration, S2C / C2S / BIDIRECTIONAL
  - `AutoPacketRegistry` – reflective ID/CODEC resolution, ServerPlayNetworking + ClientPlayNetworking binding
  - `AutoPacketRegistry.registerClientAll()` – automatic client receiver registration, no manual calls required
  - `DevkitNetworking` – `sendToPlayer()`, `sendToTracking()`, `sendToAll()`, `sendToServer()`
  - `NetworkCompat` – 1.21.1 ↔ 1.21.8+ shim (`ServerPlayNetworking.reconfigure()` detection via reflection)
- **devkit-components** – Data Components 1.21+
  - `Components.builder(modId, path, codec)` fluent API
  - `.networked()`, `.cache()`, `.persistent()`, `.build()`, `.buildAndRegister()`
  - `intComponent(modId, path)` convenience
- **devkit-config** – Server-authoritative synced config
  - `@SyncedConfig` annotation
  - `ConfigManager` – GSON loadOrCreate with auto-sync registration
  - `ConfigSyncManager` – S2C JSON sync on player join, internal `ConfigSyncPacket`
- **devkit-client** – Client bootstrap
  - `DevkitClientCore` – auto-invokes `AutoPacketRegistry.registerClientAll()`
- **devkit-datagen**
  - `TagHelper.Blocks` / `TagHelper.Items` – convention tag helpers (`c:` namespace)
  - `ModelGen` – `simpleItem()`, `handheldItem()` shortcuts
  - `LootHelper` – scaffolding for loot table DSL
  - `DevkitDataGenerator` – FabricDataGenerator entrypoint
- **devkit-fabric** – Aggregator JAR
  - Jar-in-Jar shading of all 7 modules
  - Single Modrinth/CurseForge artifact: `devkit-api`
  - `fabric.mod.json` – author: jrxmod, license Apache-2.0, environment *
- Build system
  - Multi-module Gradle, Loom 1.17-SNAPSHOT
  - Dual-branch support: 1.21.1 LTS, 1.21.8, 1.21.11
  - CI: GitHub Actions build + publish
  - Maven coordinates: `io.github.jrxmod.devkit:devkit-fabric`
  - CachyOS Linux build guide included

### Technical notes
- Zero external runtime dependencies (Fabric API only)
- MixinExtras 0.5.0 compile-only
- Target JAR size <350 KB (unobfuscated ~280 KB)
- All public API documented with professional English Javadoc
- Package namespace: `io.github.jrxmod.devkit`
- API_VERSION = 1

### Known limitations (alpha)
- `@AutoPacket` annotation scanning is explicit – requires `AutoPacketRegistry.register(MyPacket.class)` in common init. ServiceLoader auto-scan planned for 0.2.0.
- Config sync currently pushes full JSON on join only – delta sync and `/devkit reload` command planned for 0.1.0-beta.
- Data Components datagen helpers (codec JSON generation) – planned 0.1.0-beta.
- No NeoForge port yet – Fabric only as approved.

### Links
- GitHub: https://github.com/jrxmod/devkit-api (pending initial push)
- Modrinth: https://modrinth.com/mod/devkit-api
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/devkit-api
- Maven: https://maven.modrinth.com – `io.github.jrxmod.devkit`
- Author: jrxmod
- License: Apache-2.0
