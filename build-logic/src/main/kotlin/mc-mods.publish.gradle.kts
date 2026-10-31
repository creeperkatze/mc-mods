import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment

plugins {
	id("me.modmuss50.mod-publish-plugin")
}

val modId = modProperty("mod_id")
val modVersion = modProperty("mod_version")
val clientOnly = optionalModProperty("mod_environment") == "client"
// A platform is only set up when the mod has a project id for it
val modrinthId = optionalModProperty("publish_modrinth")
val curseforgeId = optionalModProperty("publish_curseforge")
val requiredMods = modPropertyList("publish_dependencies")
val sourcesJar = tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile }
val releaseChangelog = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText.map { contents ->
	val lines = contents.lines()
	val heading = Regex("^## \\[${Regex.escape(modVersion)}\\](?: - \\d{4}-\\d{2}-\\d{2})?\\s*$")
	val start = lines.indexOfFirst { heading.matches(it) }
	require(start >= 0) { "Missing changelog section for $modId $modVersion" }
	val notes = lines.drop(start + 1).takeWhile { !it.startsWith("## ") }.joinToString("\n").trim()
	require(notes.isNotBlank()) { "Empty changelog section for $modId $modVersion" }
	notes
}

publishMods {
	file = tasks.named<Jar>("jar").flatMap { it.archiveFile }
	version = project.version.toString()
	displayName = "${modProperty("mod_name")} $modVersion for ${project.minecraftVersion}"
	changelog = releaseChangelog
	type = when {
		modVersion.contains("alpha", ignoreCase = true) -> ALPHA
		Regex("beta|rc", RegexOption.IGNORE_CASE).containsMatchIn(modVersion) -> BETA
		else -> STABLE
	}
	modLoaders.add("fabric")

	if (modrinthId.isNotBlank()) {
		modrinth {
			projectId = modrinthId
			accessToken = providers.environmentVariable("MODRINTH_TOKEN")
			minecraftVersions.add(project.minecraftVersion)
			environment = if (clientOnly) ModrinthEnvironment.CLIENT_ONLY else ModrinthEnvironment.CLIENT_AND_SERVER
			additionalFile(sourcesJar) {
				type.set(SOURCES_JAR)
			}
			requiredMods.forEach { requires(it) }
		}
	}

	if (curseforgeId.isNotBlank()) {
		curseforge {
			projectId = curseforgeId
			accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
			minecraftVersions.add(project.minecraftVersion)
			client = true
			server = !clientOnly
			additionalFiles.from(sourcesJar)
			requiredMods.forEach { requires(it) }
		}
	}
}

// Fail before uploading anything when the changelog section is missing
tasks.withType<PublishModTask>().configureEach {
	val changelog = releaseChangelog
	doFirst { changelog.get() }
}
