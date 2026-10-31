# Minecraft Mods

All my Minecraft mods in one place.

![GitHub Branch Check Runs](https://img.shields.io/github/check-runs/creeperkatze/mc-mods/main)
![GitHub Issues](https://img.shields.io/github/issues/creeperkatze/mc-mods)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/creeperkatze/mc-mods)
![GitHub Repo stars](https://img.shields.io/github/stars/creeperkatze/mc-mods?style=flat)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/creeperkatze)

## 📦 Mods

| Mod                                           | Description                                              | Minecraft | Loader |
|-----------------------------------------------|----------------------------------------------------------|-----------|--------|
| [Speed Dial](speed-dial/)                     | Dial spectator and creative flight speed up to 100× | 26.3      | Fabric |

## 🛠️ Development

Any installed JDK can launch `./gradlew`. Gradle runs itself on JDK 25 and compiles the mods with the JDK set as `java` in `gradle/libs.versions.toml`, downloading either if missing.

| Command                      | What it does                                         |
|------------------------------|------------------------------------------------------|
| `./gradlew build`            | Build all mods                                       |
| `./gradlew :<mod>:runClient` | Launch a development client with the mod             |
| `./gradlew genSources`       | Decompile Minecraft for browsing its code            |

```
build-logic/                          Shared Gradle plugins (mc-mods.mod and mc-mods.publish)
gradle/libs.versions.toml             Minecraft, dependency and Gradle plugin versions
<mod>/                                One mod
```

To add a mod, create a folder with a `gradle.properties`, a `build.gradle.kts` applying `id("mc-mods.mod")`, a `CHANGELOG.md`, a `LICENSE` and `src/main/`. Add it with `include("<mod>")` in `settings.gradle.kts` and to the **Which mod?** dropdowns in `.github/ISSUE_TEMPLATE/`.
