# Changelog

All notable changes to DevKit API are documented here.

## [Unreleased]

### Target matrix

- DevKit `0.2.0-alpha.1+1.21.1` — Minecraft 1.21.1, Fabric API 0.116.13
- DevKit `0.2.0-alpha.1+1.21.8` — Minecraft 1.21.8, Fabric API 0.136.1
- DevKit `0.2.0-alpha.1+1.21.11` — Minecraft 1.21.11, Fabric API 0.141.5
- Java 21 and Fabric Loader 0.19.3 for all targets

### Added

- Added independent 1.21.8 and 1.21.11 Gradle builds without creating Git branches.
- Added version overlays for registry-key-aware items and the client datagen API introduced in later 1.21.x versions.
- Added `KRegister.register(path, key -> value)` so factories can apply the exact `RegistryKey` before constructing blocks and items.
- Added `ItemSettings` and `BlockSettings` — version-resilient fluent builders that produce the same registration code on 1.21.1, 1.21.8, and 1.21.11 through internal `ItemFactory` / `BlockFactory` overrides.
- Added `DevkitCommands` — thin Brigadier wrappers providing auto-registration, player unwrap, and `ok()`/`okBroadcast()` feedback shortcuts.
- Added `ConfigManager.reload(Class, modId, name)` to reread a config file without restarting.
- Added TestMod `/devkit_test energy`, `/devkit_test set <value>`, and `/devkit_test reload` commands to exercise networking, config save, and config reload at runtime.
- Added Modrinth Maven coordinates to README alongside the existing Maven Local instructions.
- Added a three-version GitHub Actions matrix and local `buildAllVersions` / `collectReleaseJars` tasks.

### Fixed

- Fixed ModMenu library badge format: replaced the non-functional `modmenu:badges` flat key with the standard nested `"modmenu": { "badges": ["library"] }` object.
- Replaced the deprecated `Components.intComponent(String)` null-id overload with a safe variant that defaults to the DevKit namespace.
- Wired `NetworkCompat.reconfigureIfAvailable()` into `ConfigSyncManager` so that client configuration is applied after server-authoritative configs are sent.

### Removed

- Removed TestMod version overrides for 1.21.8 and 1.21.11; `ItemSettings` makes the base file universal.

### Restored

- Restored the complete Gradle Wrapper omitted from the initial repository.
- Restored `ConfigManager`, `ConfigSyncManager`, and `SyncedConfig`; their package was accidentally excluded by the broad `config/` ignore rule.
- Included the example mod in the Gradle build.

### Changed

- Corrected Maven coordinates to `io.github.jrxmod.devkit`.
- Removed hardcoded runtime version strings.
- Made registry bootstrap explicit and retry-safe after a partially successful pass.
- Reworked packet registration validation and late client receiver wiring.
- Added client-only `DevkitClientNetworking` and `DevkitClientPacketRegistry` implementations.
- Config files are now written atomically where supported.
- Multiple configs from one mod use distinct `namespace:name` synchronization keys.
- Config payload size is checked before sending and applying.
- Replaced the null-returning loot placeholder with a usable block loot provider base.
- Removed empty mixin declarations and the non-functional automated publishing workflow.
- Updated documentation to describe only APIs that exist.
- Added production client/server smoke-test tasks; remapped environments start successfully on 1.21.1, 1.21.8, and 1.21.11.

### Known work before beta

- Verify config synchronization over a live client/server connection.
- Configure public Maven, Modrinth, and CurseForge publishing only after release destinations exist.

## [0.1.0-alpha] - 2026-07-08

Initial source publication. This revision did not build from a clean clone because wrapper and config sources were missing from Git.
