# mc-mods

Monorepo for my Minecraft mods, built with Gradle and Fabric Loom. Each mod is a top-level folder listed in `settings.gradle.kts`. All mods target the Minecraft version set in `gradle/libs.versions.toml`.

## Commands

- `./gradlew build` builds all mods, `./gradlew :<mod>:build` builds one.
- `./gradlew :<mod>:runClient` launches a development client with the mod.
- `./gradlew genSources` decompiles Minecraft. The sources jar ends up in `.gradle/loom-cache/minecraftMaven/`.
- There is no automated test suite; testing is manual in the game.

## Rules

- Prefer vanilla Minecraft APIs and built-in Gradle tooling over extra libraries and scripts.
- A mod's id, name, version and license live in its own `gradle.properties`. `fabric.mod.json` reads them through `${...}` placeholders, so never hardcode them there.
- Minecraft and dependency versions live in `gradle/libs.versions.toml`, never in a mod's `build.gradle.kts`.
- Only write a comment when the code can't explain something itself. Keep it short and simple: no em dashes, no semicolons, no nested clauses.

For code style and mod conventions, see [docs/CONVENTIONS.md](docs/CONVENTIONS.md).
For releasing and publishing, see [docs/RELEASING.md](docs/RELEASING.md).
