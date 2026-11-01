# Minecraft Mods

All my Minecraft mods in one place.

![GitHub Branch Check Runs](https://img.shields.io/github/check-runs/creeperkatze/mc-mods/main)
![GitHub Issues](https://img.shields.io/github/issues/creeperkatze/mc-mods)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/creeperkatze/mc-mods)
![GitHub Repo stars](https://img.shields.io/github/stars/creeperkatze/mc-mods?style=flat)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/creeperkatze)

## 📦 Mods

| Mod                       | Description                                                                               | Minecraft | Loader           |
| ------------------------- | ----------------------------------------------------------------------------------------- | --------- | ---------------- |
| [Speed Dial](speed-dial/) | Increase and configure your spectator and creative flight speed to fly up to 100× faster. | 26.3      | Fabric, NeoForge |

## 🛠️ Development

Any installed JDK can launch `./gradlew`. Gradle runs itself on JDK 25 and compiles the mods with the JDK set as `java` in `gradle/libs.versions.toml`, downloading either if missing.

| Command                                           | What it does                              |
| ------------------------------------------------- | ----------------------------------------- |
| `./gradlew build`                                 | Build all mods                            |
| `./gradlew :<mod>:<minecraft>-<loader>:runClient` | Launch a development client               |
| `./gradlew :<mod>:<minecraft>-fabric:genSources`  | Decompile Minecraft for browsing its code |
