# Releasing

Each mod is released on its own, tagged `<mod>/v<version>`.

## Changelog

Every mod has a `CHANGELOG.md`. Collect changes under `## [Unreleased]` as you go.

## Releasing a version

1. In `<mod>/CHANGELOG.md`, rename `## [Unreleased]` to `## [1.2.0] - 2026-11-01`.
2. Set `mod_version=1.2.0` in `<mod>/gradle.properties`.
3. Commit, tag and push:

   ```sh
   git commit -am "chore: release my-mod 1.2.0"
   git tag my-mod/v1.2.0
   git push && git push --tags
   ```

The **Release** workflow runs CI, then creates a draft GitHub release with the jars and the changelog section as notes. Review it and publish it.

## Publishing to Modrinth and CurseForge

Run the **Publish** workflow with the tag, and pick the platforms. It uploads the jar and sources jar with the changelog section as notes.

This needs, per mod, `publish_modrinth` and/or `publish_curseforge` project ids in its `gradle.properties`, and the `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN` repository secrets. Required dependencies on those platforms come from `publish_dependencies`, e.g. `publish_dependencies=fabric-api`.
