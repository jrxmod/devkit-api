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
- Added a three-version GitHub Actions matrix and local `buildAllVersions` / `collectReleaseJars` tasks.

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
