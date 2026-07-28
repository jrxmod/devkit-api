# Changelog

All notable changes to DevKit API are documented here.

## [Unreleased]

### Target

- Development version: `0.2.0-alpha.1+1.21.1`
- Minecraft 1.21.1
- Java 21
- Fabric Loader 0.19.3

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
- Added production client/server smoke-test tasks; both remapped environments start successfully on 1.21.1.

### Known work before beta

- Verify config synchronization over a live client/server connection.
- Add the Minecraft 1.21.11 build.
- Add the Minecraft 26.2 / Java 25 port.
- Configure public Maven, Modrinth, and CurseForge publishing only after release destinations exist.

## [0.1.0-alpha] - 2026-07-08

Initial source publication. This revision did not build from a clean clone because wrapper and config sources were missing from Git.
