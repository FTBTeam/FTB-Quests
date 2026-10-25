# libs/

FTB Library and FTB Teams for 26.2 are not published on maven.ftb.dev yet, so this build compiles against local jars.
Copy them here from `TCDev10/modpack` (`Modpack/mods`) before building:

- `ftb-library-neoforge-26.2.0.10.jar`
- `ftb-teams-neoforge-26.2.0.1.jar`

Then run `./gradlew :neoforge:jar` (output: `neoforge/build/libs/ftb-quests-neoforge-<version>.jar`).
