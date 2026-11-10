import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
	`java-library`
}

val modId = modProperty("mod_id")
val javaVersion = mcVersion("java").toInt()
val releaseVersion = "${modProperty("mod_version")}+$minecraftVersion"

// Tells apart version projects of different mods, which share names like 26.3-fabric
group = "${modProperty("maven_group")}.$modId"
version = "$releaseVersion-$loader"
base.archivesName = modDirectory.asFile.name

java {
	toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
	withSourcesJar()
}

repositories {
	mavenCentral()
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.isxander.dev/releases/")
	maven("https://maven.quiltmc.org/repository/release/")
}

// For the root runAll task the first mod of this target puts every other mod of it on its runtime classpath
val allTargetProjects = targetProjects(name)
if (providers.gradleProperty("runAll").orNull == name && allTargetProjects.first() == project) {
	dependencies {
		(allTargetProjects - project).forEach { other ->
			// Remapped Fabric builds expose their dev jar through Loom's namedElements
			"runtimeOnly"(if (isObfuscated && loader == "fabric") project(other.path, "namedElements") else project(other.path))
		}
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release = javaVersion
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

tasks.withType<Jar>().configureEach {
	val licenseName = "LICENSE_$modId"
	from(modDirectory.file("LICENSE")) {
		rename { licenseName }
	}
}

tasks.named<ProcessResources>("processResources") {
	val properties = mapOf(
		"version" to releaseVersion,
		"mod_id" to modId,
		"mod_folder" to modDirectory.asFile.name,
		"mod_name" to modProperty("mod_name"),
		"mod_description" to modProperty("mod_description"),
		"mod_license" to modProperty("mod_license"),
		"minecraft_version" to minecraftVersion,
		"java_version" to javaVersion.toString(),
		"fabric_loader_version" to mcVersion("fabric-loader"),
		"neoforge_version" to mcVersion("neoforge"),
		"yacl_version" to mcVersion("yacl-$loader").removeSuffix("-$loader")
	)
	inputs.properties(properties)
	filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
		expand(properties)
	}
}
