import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
	id("me.modmuss50.mod-publish-plugin")
}

val modProperties = readModProperties(layout.projectDirectory)
fun modValue(key: String) = modProperties[key].orEmpty()

val modId = modValue("mod_id")
val modName = modValue("mod_name")
val modVersion = modValue("mod_version")
val clientOnly = modValue("mod_environment") == "client"
val modrinthId = modValue("publish_modrinth")
val curseforgeId = modValue("publish_curseforge")

fun modList(key: String) = modValue(key).split(',').map(String::trim).filter(String::isNotEmpty)

fun requiredMods(loader: String) = modList("publish_dependencies") + modList("publish_dependencies_$loader")

// A jar can be published for more loaders than it is built for, e.g. Fabric jars for Quilt
fun publishLoaders(loader: String) = modList("publish_loaders_$loader").ifEmpty { listOf(loader) }

val targetProjects = childProjects.values.sortedBy { it.name }.onEach { evaluationDependsOn(it.path) }

// Local copies, so the lambda does not capture the script and stays configuration cache safe
val releaseChangelog = run {
	val id = modId
	val version = modVersion
	providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText.map { contents ->
		val lines = contents.lines()
		val heading = Regex("^## \\[${Regex.escape(version)}\\](?: - \\d{4}-\\d{2}-\\d{2})?\\s*$")
		val start = lines.indexOfFirst { heading.matches(it) }
		require(start >= 0) { "Missing changelog section for $id $version" }
		val notes = lines.drop(start + 1).takeWhile { !it.startsWith("## ") }.joinToString("\n").trim()
		require(notes.isNotBlank()) { "Empty changelog section for $id $version" }
		notes
	}
}

tasks.register("assemble") {
	group = "build"
	dependsOn(targetProjects.map { it.tasks.named("assemble") })
}

tasks.register("build") {
	group = "build"
	dependsOn(targetProjects.map { it.tasks.named("build") })
}

publishMods {
	dryRun = providers.gradleProperty("publishDryRun").map(String::toBoolean).orElse(false)
	version = modVersion
	changelog = releaseChangelog
	type = when {
		modVersion.contains("alpha", ignoreCase = true) -> ALPHA
		Regex("beta|rc", RegexOption.IGNORE_CASE).containsMatchIn(modVersion) -> BETA
		else -> STABLE
	}

	targetProjects.forEach { node ->
		val loader = node.loader
		val minecraft = node.minecraftVersion
		val loaderName = if (loader == "fabric") "Fabric" else "NeoForge"
		val targetName = "${minecraft.replace('.', '_')}$loaderName"
		val jarTask = if (node.isObfuscated && loader == "fabric") "remapJar" else "jar"
		val jar = node.tasks.named<AbstractArchiveTask>(jarTask).flatMap { it.archiveFile }
		val sources = node.tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile }
		val options = publishOptions {
			file = jar
			version = "$modVersion+$minecraft-$loader"
			displayName = "$modName $modVersion for $minecraft $loaderName"
			modLoaders.addAll(publishLoaders(loader))
		}

		if (modrinthId.isNotBlank()) {
			modrinth("modrinth$targetName") {
				from(options.get())
				projectId = modrinthId
				accessToken = providers.environmentVariable("MODRINTH_TOKEN")
				minecraftVersions.add(minecraft)
				environment = if (clientOnly) ModrinthEnvironment.CLIENT_ONLY else ModrinthEnvironment.CLIENT_AND_SERVER
				additionalFile(sources) {
					type = SOURCES_JAR
				}
				requiredMods(loader).forEach { requires(it) }
			}
		}

		if (curseforgeId.isNotBlank()) {
			curseforge("curseforge$targetName") {
				from(options.get())
				projectId = curseforgeId
				accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
				minecraftVersions.add(minecraft)
				client = true
				server = !clientOnly
				additionalFiles.from(sources)
				requiredMods(loader).forEach { requires(it) }
			}
		}
	}
}

// The changelog is a task input, so a missing section fails before anything is uploaded
tasks.withType<PublishModTask>().configureEach {
	dependsOn(targetProjects.map { it.tasks.named("assemble") })
}

listOf("Modrinth", "Curseforge").forEach { platform ->
	tasks.register("publish$platform") {
		group = "publishing"
		dependsOn(tasks.withType<PublishModTask>().matching { it.name.startsWith("publish$platform") })
	}
}
