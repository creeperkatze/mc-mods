# Code style and conventions

## Style

Tabs, LF line endings, UTF-8 and a final newline, set by `.editorconfig`. YAML and Markdown use 2 spaces.

Minecraft 26.x ships unobfuscated, so code uses Mojang's names directly (`LocalPlayer`, `Identifier`, `KeyMapping`).

## Gradle wrapper

VS Code only uses the Gradle wrapper when it trusts the wrapper's checksum. Otherwise it falls back to `java.import.gradle.version` in `.vscode/settings.json`. Keep that set to the same version as `gradle/wrapper/gradle-wrapper.properties`. Two Gradle versions building the same folders break each other's builds.

## Mod layout

```
<mod>/
  gradle.properties  Id, name, version, license, publishing ids
  build.gradle.kts   Applies mc-mods.mod and adds the mod's own dependencies
  CHANGELOG.md
  LICENSE
  src/main/
```

Client-only mods set `mod_environment=client` in `gradle.properties` and `"environment": "client"` in `fabric.mod.json`.

## Mixins

- Prefix every injector method with the mod's name in camel case and a `$`, e.g. `myMod$scrollSpeed`, so it can't clash with other mods.
- Prefer MixinExtras injectors like `@WrapOperation` over `@Redirect` and `@Overwrite`, they stay compatible with other mods touching the same code.
- Keep logic out of mixins. A mixin hands off to a regular class of the mod.

## Translations

Every key starts with the mod id, e.g. `my-mod.config.max_speed`. Keybinds use `key.<mod-id>.<name>`.

## Keybinds

Every mod registers its keybinds in its own category, never in a vanilla one. Register it with `KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"))` and name it with the `key.category.<mod-id>.main` translation key.

## Config

Settings are plain JSON read with Gson from `config/<mod-id>.json`. The settings screen is a vanilla `OptionsSubScreen`, opened through Mod Menu as an optional dependency.

## Logging

Use the mod's `LOGGER`, an SLF4J logger named after the mod id.
