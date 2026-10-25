# Port to Minecraft 26.2 / NeoForge 26.2.0.88

Based on upstream FTBTeam/FTB-Quests `main` @ 3fc875bb (26.1.2, JSON5 quest format).

- NeoForge only: the Fabric module and the upstream publishing workflows were removed. `common` is compiled with ModDevGradle against NeoForge.
- Targets MC 26.2, NeoForge `[26.2.0.88,27)`, FTB Library `26.2.0.10+`, FTB Teams `26.2.0.1+` (local jars, see `libs/README.md`).
- The Fabric access widener is replaced by `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. `CompoundTag` is sealed in 26.2, so the `extendable` entry was dropped (nothing extends it).
- 26.2 API changes:
  - `Minecraft#setScreen` / `Minecraft#screen` / `getToastManager()` → `Minecraft#gui.setScreen()` / `gui.screen()` / `gui.toastManager()`
  - `EntityType.PLAYER/PIG` → `EntityTypes.*`
  - `BlockPos#getBottomCenter()` → `Vec3.atBottomCenterOf()`
  - `Properties#emissiveRendering` now takes a `Predicate<BlockState>`
  - `ChatFormatting#getChar()/getColor()` removed → `toString()` / `TextColor.fromLegacyFormat()`
- Gametests (neoforge/src/test) are not ported and are disabled.
