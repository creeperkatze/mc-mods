# Minecraft Mods

All my Minecraft mods in one place.

![GitHub Branch Check Runs](https://img.shields.io/github/check-runs/creeperkatze/mc-mods/main)
![GitHub Issues](https://img.shields.io/github/issues/creeperkatze/mc-mods)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/creeperkatze/mc-mods)
[![Crowdin](https://badges.crowdin.net/creeperkatze-mc-mods/localized.svg)](https://crowdin.com/project/creeperkatze-mc-mods)
![GitHub Repo stars](https://img.shields.io/github/stars/creeperkatze/mc-mods?style=flat)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/creeperkatze)

## 📦 Mods

| Mod                       | Description                                                                                                                                 | Downloads                                                                                                                                                                                                                                                                | Minecraft                 | Loaders                                                                                                                                                                                                                                                              |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [Accelerate](accelerate/) | Change and configure your creative and spectator fly speed with the scroll wheel to fly up to 1000x faster.                                 | [![Modrinth](https://img.shields.io/modrinth/dt/MEVdpviL?logo=modrinth&label=&color=555)](https://modrinth.com/mod/MEVdpviL) [![CurseForge](https://img.shields.io/curseforge/dt/1778908?logo=curseforge&label=&color=555)](https://www.curseforge.com/projects/1778908) | `1.21.11`, `26.2`, `26.3` | <img src=".github/assets/loaders/fabric.svg" alt="Fabric" title="Fabric" height="16"> <img src=".github/assets/loaders/quilt.svg" alt="Quilt" title="Quilt" height="16"> <img src=".github/assets/loaders/neoforge.svg" alt="NeoForge" title="NeoForge" height="16"> |
| [Rearrange](rearrange/)   | Drag and drop to reorder and sort your multiplayer server list, singleplayer worlds, resource packs and data packs.                         | [![Modrinth](https://img.shields.io/modrinth/dt/LI06Bzs9?logo=modrinth&label=&color=555)](https://modrinth.com/mod/LI06Bzs9) [![CurseForge](https://img.shields.io/curseforge/dt/1780189?logo=curseforge&label=&color=555)](https://www.curseforge.com/projects/1780189) | `1.21.11`, `26.2`, `26.3` | <img src=".github/assets/loaders/fabric.svg" alt="Fabric" title="Fabric" height="16"> <img src=".github/assets/loaders/quilt.svg" alt="Quilt" title="Quilt" height="16"> <img src=".github/assets/loaders/neoforge.svg" alt="NeoForge" title="NeoForge" height="16"> |
| [Organize](organize/)     | Sort your resource packs into folders and browse them in the pack screen.                                                                   | [![Modrinth](https://img.shields.io/modrinth/dt/JC5JBj2W?logo=modrinth&label=&color=555)](https://modrinth.com/mod/JC5JBj2W) [![CurseForge](https://img.shields.io/curseforge/dt/1780188?logo=curseforge&label=&color=555)](https://www.curseforge.com/projects/1780188) | `1.21.11`, `26.2`, `26.3` | <img src=".github/assets/loaders/fabric.svg" alt="Fabric" title="Fabric" height="16"> <img src=".github/assets/loaders/quilt.svg" alt="Quilt" title="Quilt" height="16"> <img src=".github/assets/loaders/neoforge.svg" alt="NeoForge" title="NeoForge" height="16"> |
| [Gather](gather/)         | Host your world for friends with offline mode, more players, a custom server name and more LAN options.                                     |                                                                                                                                                                                                                                                                          | `26.2`, `26.3`            | <img src=".github/assets/loaders/fabric.svg" alt="Fabric" title="Fabric" height="16"> <img src=".github/assets/loaders/quilt.svg" alt="Quilt" title="Quilt" height="16"> <img src=".github/assets/loaders/neoforge.svg" alt="NeoForge" title="NeoForge" height="16"> |
| [Illuminate](illuminate/) | Bring your room into the game with ambient RGB lighting for biomes, weather, damage, status effects and more through OpenRGB and SignalRGB. | [![Modrinth](https://img.shields.io/modrinth/dt/i9Haq2Rw?logo=modrinth&label=&color=555)](https://modrinth.com/mod/i9Haq2Rw) [![CurseForge](https://img.shields.io/curseforge/dt/1795192?logo=curseforge&label=&color=555)](https://www.curseforge.com/projects/1795192) | `1.21.11`, `26.2`, `26.3` | <img src=".github/assets/loaders/fabric.svg" alt="Fabric" title="Fabric" height="16"> <img src=".github/assets/loaders/quilt.svg" alt="Quilt" title="Quilt" height="16"> <img src=".github/assets/loaders/neoforge.svg" alt="NeoForge" title="NeoForge" height="16"> |
| [Resume](resume/)         | See where you left off while your world or server loads, with a screenshot of the moment you quit as the loading screen.                    |                                                                                                                                                                                                                                                                          | `1.21.11`, `26.2`, `26.3` | <img src=".github/assets/loaders/fabric.svg" alt="Fabric" title="Fabric" height="16"> <img src=".github/assets/loaders/quilt.svg" alt="Quilt" title="Quilt" height="16"> <img src=".github/assets/loaders/neoforge.svg" alt="NeoForge" title="NeoForge" height="16"> |

## 🌐 Translating

Translations are managed on [Crowdin](https://crowdin.com/project/creeperkatze-mc-mods). You can contribute without any technical knowledge, just pick your language and start translating.

New translations are automatically pulled every Monday.

## 🛠️ Development

Any installed JDK can launch `./gradlew`. Gradle runs itself on JDK 25 and compiles each Minecraft version with the JDK set as `java` in its `gradle/minecraft/<minecraft>.versions.toml`, downloading any that are missing.

| Command                                           | What it does                              |
| ------------------------------------------------- | ----------------------------------------- |
| `./gradlew build`                                 | Build all mods                            |
| `./gradlew :<mod>:<minecraft>-<loader>:runClient` | Launch a development client               |
| `./gradlew runAll -PrunAll=<minecraft>-<loader>`  | Launch a client with every mod            |
| `./gradlew :<mod>:<minecraft>-fabric:genSources`  | Decompile Minecraft for browsing its code |
