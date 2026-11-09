# Minecraft Mods

All my Minecraft mods in one place.

![GitHub Branch Check Runs](https://img.shields.io/github/check-runs/creeperkatze/mc-mods/main)
![GitHub Issues](https://img.shields.io/github/issues/creeperkatze/mc-mods)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/creeperkatze/mc-mods)
![GitHub Repo stars](https://img.shields.io/github/stars/creeperkatze/mc-mods?style=flat)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/creeperkatze)

## 📦 Mods

| Mod                       | Description                                                                                                                                 | Downloads                                                                                                                                                                                                                                                    | Minecraft           | Loaders                                                                                                                                                                                                                   |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [Accelerate](accelerate/) | Change and configure your creative and spectator fly speed with the scroll wheel to fly up to 100x faster.                                  | [![Modrinth](https://modfolio.creeperkatze.dev/modrinth/project/MEVdpviL/downloads)](https://modrinth.com/mod/MEVdpviL) [![CurseForge](https://modfolio.creeperkatze.dev/curseforge/project/1778908/downloads)](https://www.curseforge.com/projects/1778908) | 1.21.11, 26.2, 26.3 | <img src=".github/assets/loaders/fabric.svg" alt="" height="16"> Fabric <img src=".github/assets/loaders/quilt.svg" alt="" height="16"> Quilt <img src=".github/assets/loaders/neoforge.svg" alt="" height="16"> NeoForge |
| [Rearrange](rearrange/)   | Drag and drop to reorder and sort your multiplayer server list, singleplayer worlds, resource packs and data packs.                         | [![Modrinth](https://modfolio.creeperkatze.dev/modrinth/project/LI06Bzs9/downloads)](https://modrinth.com/mod/LI06Bzs9) [![CurseForge](https://modfolio.creeperkatze.dev/curseforge/project/1780189/downloads)](https://www.curseforge.com/projects/1780189) | 1.21.11, 26.2, 26.3 | <img src=".github/assets/loaders/fabric.svg" alt="" height="16"> Fabric <img src=".github/assets/loaders/quilt.svg" alt="" height="16"> Quilt <img src=".github/assets/loaders/neoforge.svg" alt="" height="16"> NeoForge |
| [Organize](organize/)     | Sort your resource packs into folders and browse them in the pack screen.                                                                   | [![Modrinth](https://modfolio.creeperkatze.dev/modrinth/project/JC5JBj2W/downloads)](https://modrinth.com/mod/JC5JBj2W) [![CurseForge](https://modfolio.creeperkatze.dev/curseforge/project/1780188/downloads)](https://www.curseforge.com/projects/1780188) | 1.21.11, 26.2, 26.3 | <img src=".github/assets/loaders/fabric.svg" alt="" height="16"> Fabric <img src=".github/assets/loaders/quilt.svg" alt="" height="16"> Quilt <img src=".github/assets/loaders/neoforge.svg" alt="" height="16"> NeoForge |
| [Gather](gather/)         | Host your world for friends with offline mode, more players, a custom server name and more LAN options.                                     |                                                                                                                                                                                                                                                              | 26.3                | <img src=".github/assets/loaders/fabric.svg" alt="" height="16"> Fabric <img src=".github/assets/loaders/quilt.svg" alt="" height="16"> Quilt <img src=".github/assets/loaders/neoforge.svg" alt="" height="16"> NeoForge |
| [Illuminate](illuminate/) | Bring your room into the game with ambient RGB lighting for biomes, weather, damage, status effects and more through OpenRGB and SignalRGB. |                                                                                                                                                                                                                                                              | 26.3                | <img src=".github/assets/loaders/fabric.svg" alt="" height="16"> Fabric <img src=".github/assets/loaders/quilt.svg" alt="" height="16"> Quilt <img src=".github/assets/loaders/neoforge.svg" alt="" height="16"> NeoForge |

## 🛠️ Development

Any installed JDK can launch `./gradlew`. Gradle runs itself on JDK 25 and compiles each Minecraft version with the JDK set as `java` in its `gradle/minecraft/<minecraft>.versions.toml`, downloading any that are missing.

| Command                                           | What it does                              |
| ------------------------------------------------- | ----------------------------------------- |
| `./gradlew build`                                 | Build all mods                            |
| `./gradlew :<mod>:<minecraft>-<loader>:runClient` | Launch a development client               |
| `./gradlew :<mod>:<minecraft>-fabric:genSources`  | Decompile Minecraft for browsing its code |
